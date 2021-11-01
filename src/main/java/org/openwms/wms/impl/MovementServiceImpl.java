/*
 * Copyright 2005-2021 the original author or authors.
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
package org.openwms.wms.impl;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.exception.BusinessRuntimeException;
import org.ameba.exception.NotFoundException;
import org.ameba.i18n.Translator;
import org.ameba.mapping.BeanMapper;
import org.openwms.common.location.LocationPK;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationGroupVO;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.Barcode;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.wms.MovementService;
import org.openwms.wms.api.MovementState;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.MovementVO;
import org.openwms.wms.spi.DefaultMovementState;
import org.openwms.wms.spi.MovementStateResolver;
import org.openwms.wms.spi.MovementTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.ameba.system.ValidationUtil.validate;
import static org.openwms.wms.MovementsMessages.LOCATION_NOT_FOUND_BY_ERP_CODE;
import static org.openwms.wms.MovementsMessages.LOCATION_NOT_FOUND_BY_ID;
import static org.openwms.wms.MovementsMessages.MOVEMENT_COMPLETED_NOT_MOVED;
import static org.openwms.wms.MovementsMessages.MOVEMENT_NOT_FOUND;

/**
 * A MovementServiceImpl is a Spring managed transaction service that deals with {@link Movement}s.
 *
 * @author Heiko Scherrer
 */
@Validated
@TxService
class MovementServiceImpl implements MovementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovementServiceImpl.class);
    private final BeanMapper mapper;
    private final Validator validator;
    private final Translator translator;
    private final MovementStateResolver movementStateResolver;
    private final MovementRepository repository;
    private final MovementTypeResolver movementTypeResolver;
    private final Map<MovementType, MovementHandler> handlers;
    private final TransportUnitApi transportUnitApi;
    private final LocationApi locationApi;
    private final LocationGroupApi locationGroupApi;

    MovementServiceImpl(List<MovementHandler> handlersList, BeanMapper mapper, Validator validator,
            Translator translator, MovementStateResolver movementStateResolver, MovementRepository repository, @Autowired(required = false) MovementTypeResolver movementTypeResolver,
            TransportUnitApi transportUnitApi, LocationApi locationApi, LocationGroupApi locationGroupApi) {
        this.handlers = handlersList.stream().collect(Collectors.toMap(MovementHandler::getType, h -> h));
        this.mapper = mapper;
        this.validator = validator;
        this.translator = translator;
        this.movementStateResolver = movementStateResolver;
        this.repository = repository;
        this.movementTypeResolver = movementTypeResolver;
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
    public MovementVO create(
            @NotEmpty(groups = ValidationGroups.Movement.Create.class) String bk,
            @NotNull(groups = ValidationGroups.Movement.Create.class) @Valid MovementVO vo) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create a Movement for [{}] with data [{}]", bk, vo);
        }
        validateAndResolveType(vo);
        MovementHandler movementHandler = handlers.get(vo.getType());
        if (movementHandler == null) {
            throw new IllegalArgumentException(format("No handler registered for MovementType [%s]", vo.getType()));
        }
        validate(validator, vo, ValidationGroups.Movement.Create.class);
        TransportUnitVO transportUnit = resolveTransportUnit(bk).orElseThrow(() -> new NotFoundException(format("TransportUnit with BK [%s] does not exist", bk)));
        LocationVO sourceLocation = resolveLocation(vo);
        Movement movement = mapper.map(vo, Movement.class);
        locationApi.findLocationByErpCode(vo.getTarget()).ifPresent( loc -> movement.setTargetLocation(loc.getErpCode()));
        movement.setSourceLocation(sourceLocation.getErpCode());
        movement.setSourceLocationGroupName(sourceLocation.getLocationGroupName());
        movement.setTransportUnitBk(Barcode.of(bk));
        movement.setState(movementStateResolver.getNewState());
        validate(validator, movement, ValidationGroups.Movement.Create.class);
        Movement result = movementHandler.create(movement);
        return convert(result);
    }

    private Optional<TransportUnitVO> resolveTransportUnit(String bk) {
        try {
            return Optional.of(transportUnitApi.findTransportUnit(bk));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private LocationVO resolveLocation(MovementVO vo) {
        if (LocationPK.isValid(vo.getSourceLocation())) {
            try {
                return locationApi.findLocationByCoordinate(vo.getSourceLocation())
                        .orElseThrow(NotFoundException::new);
            } catch (Exception e) {
                throw new NotFoundException(translator, LOCATION_NOT_FOUND_BY_ID, new String[]{vo.getSourceLocation()}, vo.getSourceLocation());
            }
        } else {
            try {
                return locationApi.findLocationByErpCode(vo.getSourceLocation()).orElseThrow(NotFoundException::new);
            } catch (Exception e) {
                throw new NotFoundException(translator, LOCATION_NOT_FOUND_BY_ERP_CODE, new String[]{vo.getSourceLocation()}, vo.getSourceLocation());
            }
        }
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
    public List<MovementVO> findFor(@NotNull MovementState state, @NotEmpty String source, @NotNull MovementType... types) {
        Optional<LocationGroupVO> locationGroupOpt = locationGroupApi.findByName(source);
        List<String> sources;
        sources = locationGroupOpt.map(lg -> lg
                .streamLocationGroups()
                .map(LocationGroupVO::getName)
                .collect(Collectors.toList()))
                .orElseGet(() -> Collections.singletonList(source));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Search for Movements of type [{}] in state [{}] and in [{}]", types, state, sources);
        }

        return Arrays.stream(types)
                .parallel()
                .map(t -> handlers.get(t).findInStateAndSource(state, sources))
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
    public MovementVO move(@NotEmpty String pKey, @Valid @NotNull MovementVO vo) {
        Movement movement = findInternal(pKey);
        if (movement.getState() == movementStateResolver.getCompletedState()) {
            throw new BusinessRuntimeException(translator, MOVEMENT_COMPLETED_NOT_MOVED, new String[]{pKey}, pKey);
        }
        movement.setState(DefaultMovementState.valueOf(vo.getState()));
        if (movement.getStartDate() == null) {
            movement.setStartDate(ZonedDateTime.now());
        }
        if (vo.hasTransportUnitBK()) {
            movement.setTransportUnitBk(Barcode.of(vo.getTransportUnitBk()));
        }
        LocationVO sourceLocation = resolveLocation(vo);
        movement.setSourceLocation(sourceLocation.getErpCode());
        movement.setSourceLocationGroupName(sourceLocation.getLocationGroupName());
        movement = repository.save(movement);
        return convert(movement);
    }

    /**
     * {@inheritDoc}
     */
    @Validated(ValidationGroups.Movement.Complete.class)
    @Measured
    @Override
    public MovementVO complete(@NotEmpty String pKey, @Valid @NotNull MovementVO vo) {
        LOGGER.debug("Got request to complete movement [{}]", vo);
        String transportUnit = vo.getTransportUnitBk();
        Movement movement = findInternal(pKey);
        var location = locationApi.findLocationByErpCode(vo.getTarget()).orElseThrow(() -> new NotFoundException(format("Location with ERP Code [%s] does not exist", vo.getTarget())));
        transportUnitApi.moveTU(vo.hasTransportUnitBK()
                        ? vo.getTransportUnitBk()
                        : movement.getTransportUnitBk().getValue()
                , location.getLocationId());
        movement.setState(movementStateResolver.getCompletedState());
        movement.setEndDate(ZonedDateTime.now());
        movement.setTargetLocation(vo.getTarget());
        movement.setTargetLocationGroup(vo.getTarget());
        movement = repository.save(movement);
        LOGGER.debug("Completed movement [{}]: ", movement);
        return convert(movement);
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public MovementVO cancel(@NotEmpty String pKey) {
        Movement movement = findInternal(pKey);
        if (movement.getState() != DefaultMovementState.DONE) {
            movement.setState(DefaultMovementState.CANCELLED);
            movement = repository.save(movement);
            LOGGER.debug("Cancelled movement [{}]: ", movement);
        }
        return convert(movement);
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public List<MovementVO> findAll() {
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
    public List<MovementVO> findForTU(@NotEmpty String barcode) {
        var all = repository.findByTransportUnitBkAndStateIsNot(Barcode.of(barcode), DefaultMovementState.DONE);
        if (all.isEmpty()) {
            LOGGER.debug("No Movements for TU [{}] in active state", barcode);
            return Collections.emptyList();
        }
        LOGGER.debug("Movements for TU [{}] in active state exist", barcode);
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
