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
import org.ameba.exception.NotFoundException;
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
    private final MovementStateResolver movementStateProvider;
    private final MovementRepository repository;
    private final MovementTypeResolver movementTypeResolver;
    private final Map<MovementType, MovementHandler> handlers;
    private final TransportUnitApi transportUnitApi;
    private final LocationApi locationApi;
    private final LocationGroupApi locationGroupApi;

    MovementServiceImpl(List<MovementHandler> handlersList, BeanMapper mapper, Validator validator,
            MovementStateResolver movementStateProvider, MovementRepository repository, @Autowired(required = false) MovementTypeResolver movementTypeResolver,
            TransportUnitApi transportUnitApi, LocationApi locationApi, LocationGroupApi locationGroupApi) {
        this.handlers = handlersList.stream().collect(Collectors.toMap(MovementHandler::getType, h -> h));
        this.mapper = mapper;
        this.validator = validator;
        this.movementStateProvider = movementStateProvider;
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
        movement.setSourceLocation(sourceLocation.getErpCode());
        movement.setSourceLocationGroupName(sourceLocation.getLocationGroupName());
        movement.setTransportUnitBk(Barcode.of(bk));
        movement.setState(movementStateProvider.getNewState().getName());
        movement.setTargetLocationGroup(movement.getTargetLocation());
        movement.setTargetLocation(null);
        validate(validator, movement, ValidationGroups.Movement.Create.class);
        Movement result = movementHandler.create(movement);
        return mapper.map(result, MovementVO.class);
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
                return locationApi.findLocationByCoordinate(vo.getSourceLocation()).orElseThrow(() -> new NotFoundException(format("Location with locationId [%s] does not exist", vo.getSourceLocation())));
            } catch (Exception e) {
                throw new NotFoundException(format("Location with locationId [%s] does not exist", vo.getSourceLocation()));
            }
        } else {
            try {
                return locationApi.findLocationByErpCode(vo.getSourceLocation()).orElseThrow(() -> new NotFoundException(format("Location with erpCode [%s] does not exist", vo.getSourceLocation())));
            } catch (Exception e) {
                throw new NotFoundException(format("Location with erpCode [%s] does not exist", vo.getSourceLocation()));
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

        return mapper.map(Arrays.stream(types)
                .parallel()
                .map(t -> handlers.get(t).findInStateAndSource(state, sources))
                .reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                }), MovementVO.class);
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

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public MovementVO move(@NotEmpty String pKey, @NotNull MovementVO vo) {
        Movement movement = repository.findBypKey(pKey).orElseThrow(() -> new NotFoundException(format("Movement with pKey [%s] does not exist", pKey)));
        movement.setState(vo.getState());
        if (movement.getStartDate() == null) {
            movement.setStartDate(ZonedDateTime.now());
        }
        LocationVO sourceLocation = resolveLocation(vo);
        movement.setSourceLocation(sourceLocation.getErpCode());
        movement.setSourceLocationGroupName(sourceLocation.getLocationGroupName());
        movement = repository.save(movement);
        return mapper.map(movement, MovementVO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public MovementVO complete(@NotEmpty String pKey, @NotNull MovementVO vo) {
        Movement movement = repository.findBypKey(pKey).orElseThrow(() -> new NotFoundException(format("Movement with pKey [%s] does not exist", pKey)));
        movement.setState(movementStateProvider.getCompletedState().getName());
        movement.setEndDate(ZonedDateTime.now());
        movement.setTargetLocation(vo.getTarget());
        movement.setTargetLocationGroup(vo.getTarget());
        movement = repository.save(movement);
        return mapper.map(movement, MovementVO.class);
    }
}
