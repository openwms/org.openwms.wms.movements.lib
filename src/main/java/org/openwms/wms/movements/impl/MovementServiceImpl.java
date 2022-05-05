/*
 * Copyright 2005-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.wms.movements.impl;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.exception.BusinessRuntimeException;
import org.ameba.exception.NotFoundException;
import org.ameba.exception.ServiceLayerException;
import org.ameba.i18n.Translator;
import org.ameba.mapping.BeanMapper;
import org.openwms.common.location.LocationPK;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.Barcode;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.wms.movements.MovementService;
import org.openwms.wms.movements.api.MovementState;
import org.openwms.wms.movements.api.MovementType;
import org.openwms.wms.movements.api.MovementVO;
import org.openwms.wms.movements.spi.DefaultMovementState;
import org.openwms.wms.movements.spi.MovementStateResolver;
import org.openwms.wms.movements.spi.MovementTypeResolver;
import org.openwms.wms.movements.spi.Validators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.ameba.system.ValidationUtil.validate;
import static org.openwms.wms.movements.MovementsMessages.LOCATION_NOT_FOUND_BY_ERP_CODE;
import static org.openwms.wms.movements.MovementsMessages.LOCATION_NOT_FOUND_BY_ID;
import static org.openwms.wms.movements.MovementsMessages.MOVEMENT_COMPLETED_NOT_MOVED;
import static org.openwms.wms.movements.MovementsMessages.MOVEMENT_NOT_FOUND;

/**
 * A MovementServiceImpl is a Spring managed transaction service that deals with {@link Movement}s.
 *
 * @author Heiko Scherrer
 */
@Validated
@TxService
class MovementServiceImpl implements MovementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovementServiceImpl.class);
    private final ApplicationEventPublisher eventPublisher;
    private final BeanMapper mapper;
    private final Validator validator;
    private final Translator translator;
    private final MovementStateResolver movementStateResolver;
    private final MovementRepository repository;
    private final MovementTypeResolver movementTypeResolver;
    private final PluginRegistry<MovementHandler, MovementType> handlers;
    private final Validators validators;
    private final TransportUnitApi transportUnitApi;
    private final LocationApi locationApi;
    private final LocationGroupApi locationGroupApi;

    MovementServiceImpl(ApplicationEventPublisher eventPublisher, BeanMapper mapper, Validator validator, Translator translator,
                        MovementStateResolver movementStateResolver, MovementRepository repository,
                        @Autowired(required = false) MovementTypeResolver movementTypeResolver,
                        PluginRegistry<MovementHandler, MovementType> handlers,
                        Validators validators, TransportUnitApi transportUnitApi, LocationApi locationApi, LocationGroupApi locationGroupApi) {
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.validator = validator;
        this.translator = translator;
        this.movementStateResolver = movementStateResolver;
        this.repository = repository;
        this.movementTypeResolver = movementTypeResolver;
        this.handlers = handlers;
        this.validators = validators;
        this.transportUnitApi = transportUnitApi;
        this.locationApi = locationApi;
        this.locationGroupApi = locationGroupApi;
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Validated(ValidationGroups.Movement.Create.class)
    @Override
    public @NotNull MovementVO create(
            @NotBlank(groups = ValidationGroups.Movement.Create.class) String bk,
            @NotNull(groups = ValidationGroups.Movement.Create.class) @Valid MovementVO vo) {
        LOGGER.debug("Create a Movement for [{}] with data [{}]", bk, vo);
        validateAndResolveType(vo);
        var movementHandler = resolveHandler(vo.getType());
        resolveTransportUnit(bk);
        var sourceLocation = resolveLocation(vo.getSourceLocation());
        var movement = mapper.map(vo, Movement.class);
        try {
            resolveLocation(vo.getTarget());
        } catch ( NotFoundException nfe) {
            LOGGER.debug("The Movement has no valid target [{}] set, trying to resolve it later", vo.getTarget());
        }
        movement.setInitiatorOrDefault(movement.getInitiator(), "n/a");
        movement.setSourceLocation(sourceLocation.getErpCode());
        movement.setSourceLocationGroupName(sourceLocation.getLocationGroupName());
        movement.setTransportUnitBk(Barcode.of(bk));
        movement.setState(movementStateResolver.getNewState());
        validate(validator, movement, ValidationGroups.Movement.Create.class);
        var result = movementHandler.create(movement);
        return convert(result);
    }

    private MovementHandler resolveHandler(MovementType type) {
        var movementHandler = handlers.getPluginFor(type);
        if (movementHandler.isEmpty()) {
            throw new IllegalArgumentException(format("No handler registered for MovementType [%s]", type));
        }
        return movementHandler.get();
    }

    private void resolveTransportUnit(String bk) {
        try {
            transportUnitApi.findTransportUnit(bk);
        } catch (Exception ex) {
            throw new ServiceLayerException(ex.getMessage(), ex);
        }
    }

    private LocationVO resolveLocation(String locationIdentifier) {
        Optional<LocationVO> optLocation;
        if (LocationPK.isValid(locationIdentifier)) {
            try {
                optLocation = locationApi.findById(locationIdentifier);
            } catch (Exception ex) {
                // Any technical reasons
                throw new ServiceLayerException(ex.getMessage(), ex);
            }
            if (optLocation.isEmpty()) {
                throw new NotFoundException(translator, LOCATION_NOT_FOUND_BY_ID, new String[]{locationIdentifier}, locationIdentifier);
            }
        } else {
            try {
                optLocation = locationApi.findByErpCode(locationIdentifier);
            } catch (Exception ex) {
                // Any technical reasons
                throw new ServiceLayerException(ex.getMessage(), ex);
            }
            if (optLocation.isEmpty()) {
                throw new NotFoundException(translator, LOCATION_NOT_FOUND_BY_ERP_CODE, new String[]{locationIdentifier}, locationIdentifier);
            }
        }
        return optLocation.get();
    }

    private void validateAndResolveType(MovementVO vo) {
        if (vo.getType() == null) {
            if (!vo.hasTarget()) {
                throw new IllegalArgumentException("Can't automatically resolve a MovementType because no target is set");
            }
            if (movementTypeResolver == null) {
                throw new IllegalStateException("No type is set and needs to be resolved but no MovementTypeResolver is configured");
            }
            vo.setType(movementTypeResolver.resolve(vo.getTransportUnitBk(), vo.getTarget())
                    .orElseThrow(() -> new IllegalArgumentException(format("Can't resolve MovementType for TransportUnit [%s] from target [%s]",
                            vo.getTransportUnitBk(), vo.getTarget()))));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public List<MovementVO> findFor(@NotNull MovementState state, @NotBlank String source, @NotEmpty MovementType... types) {
        var sources = locationGroupApi.findByName(source)
                .map(lg -> lg.streamLocationGroups().map(LocationGroupVO::getName).collect(Collectors.toList()))
                .orElseGet(() -> Collections.singletonList(source));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Search for Movements of types [{}] in state [{}] and in source [{}]", types, state, sources);
        }
        return Arrays.stream(types)
                .parallel()
                .map(t -> resolveHandler(t).findInStateAndSource(state, sources))
                .reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                }).stream().map(this::convert).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public List<String> getPriorityList() {
        return Arrays.stream(PriorityLevel.values())
                .filter(Objects::nonNull)
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    private Movement findInternal(String pKey) {
        return repository.findBypKey(pKey)
                .orElseThrow(() -> new NotFoundException(translator, MOVEMENT_NOT_FOUND, new String[]{pKey}, pKey));
    }

    /**
     * {@inheritDoc}
     */
    @Validated(ValidationGroups.Movement.Move.class)
    @Measured
    @Override
    public @NotNull MovementVO move(@NotBlank String pKey, @Valid @NotNull MovementVO vo) {
        var movement = findInternal(pKey);
        movement = validators.onMove(movement, vo.getSourceLocation(), mapper.map(vo, Movement.class));
        if (movement.getState() == movementStateResolver.getCompletedState()) {
            throw new BusinessRuntimeException(translator, MOVEMENT_COMPLETED_NOT_MOVED, new String[]{pKey}, pKey);
        }
        movement.setState(DefaultMovementState.valueOf(vo.getState()));
        movement.initStartDate(ZonedDateTime.now());
        var sourceLocation = resolveLocation(vo.getSourceLocation());
        if (vo.hasTransportUnitBK()) {
            movement.setTransportUnitBk(Barcode.of(vo.getTransportUnitBk()));
        }
        transportUnitApi.moveTU(movement.getTransportUnitBk().getValue(), sourceLocation.getLocationId());
        var previousLocation = movement.getSourceLocation();
        movement.setSourceLocation(sourceLocation.getErpCode());
        movement.setSourceLocationGroupName(sourceLocation.getLocationGroupName());
        LOGGER.debug("Moving Movement [{}]", movement);
        movement = repository.save(movement);
        eventPublisher.publishEvent(new MovementEvent(movement, MovementEvent.Type.MOVED, previousLocation));
        return convert(movement);
    }

    /**
     * {@inheritDoc}
     */
    @Validated(ValidationGroups.Movement.Complete.class)
    @Measured
    @Override
    public @NotNull MovementVO complete(@NotBlank String pKey, @Valid @NotNull MovementVO vo) {
        LOGGER.debug("Got request to complete Movement with pKey [{}], [{}]", pKey, vo);
        var movement = findInternal(pKey);
        movement = validators.onMove(movement, vo.getTarget(), mapper.map(vo, Movement.class));
        if (movement.getState().ordinal() < DefaultMovementState.DONE.ordinal()) {
            var location = resolveLocation(vo.getTarget());
            transportUnitApi.moveTU(vo.hasTransportUnitBK()
                    ? vo.getTransportUnitBk()
                    : movement.getTransportUnitBk().getValue(), location.getLocationId());
            movement.setState(movementStateResolver.getCompletedState());
            movement.setEndDate(ZonedDateTime.now());
            var previousLocation = location.getErpCode();
            movement.setTargetLocation(vo.getTarget());
            movement.setTargetLocationGroup(vo.getTarget());
            movement = repository.save(movement);
            eventPublisher.publishEvent(new MovementEvent(movement, MovementEvent.Type.COMPLETED, previousLocation));
        } else {
            LOGGER.info("Movement [{}] is already in state [{}] and cannot be completed", pKey, movement.getState());
        }
        return convert(movement);
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public @NotNull MovementVO cancel(@NotBlank String pKey) {
        LOGGER.debug("Got request to cancel Movement with pKey [{}]", pKey);
        var movement = findInternal(pKey);
        if (movement.getState().ordinal() < DefaultMovementState.CANCELLED.ordinal()) {
            movement.setState(DefaultMovementState.CANCELLED);
            movement.setEndDate(ZonedDateTime.now());
            movement = repository.save(movement);
            eventPublisher.publishEvent(new MovementEvent(movement, MovementEvent.Type.CANCELLED));
        } else {
            LOGGER.info("Movement [{}] is already in state [{}] and cannot be cancelled", pKey, movement.getState());
        }
        return convert(movement);
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public @NotNull List<MovementVO> findAll() {
        var all = repository.findAll();
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        return all.stream().map(this::convert).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public List<MovementVO> findForTuAndTypesAndStates(@NotBlank String barcode, @NotEmpty List<String> types, @NotEmpty List<String> states) {
        var all = repository.findByTransportUnitBkAndTypeInAndStateIn(
                Barcode.of(barcode),
                types.stream().map(MovementType::valueOf).collect(Collectors.toList()),
                states.stream().map(DefaultMovementState::valueOf).collect(Collectors.toList())
        );
        if (all.isEmpty()) {
            LOGGER.debug("No Movements for TU [{}] in states [{}]", barcode, states);
            return Collections.emptyList();
        }
        LOGGER.debug("Movements for TU [{}] in states [{}] exist", barcode, states);
        return all.stream().map(this::convert).collect(Collectors.toList());
    }

    private MovementVO convert(Movement eo) {
        var vo = mapper.map(eo, MovementVO.class);
        if (eo.getTargetLocation() != null && !eo.getTargetLocation().isEmpty()) {
            vo.setTarget(eo.getTargetLocation());
        }
        return vo;
    }
}
