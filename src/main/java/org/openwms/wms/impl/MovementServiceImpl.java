/*
 * Copyright 2005-2020 the original author or authors.
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
import org.openwms.common.transport.Barcode;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.wms.MovementService;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.MovementVO;
import org.openwms.wms.spi.MovementTypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;

import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.ameba.system.ValidationUtil.validate;

/**
 * A MovementServiceImpl.
 *
 * @author Heiko Scherrer
 */
@Validated
@TxService
class MovementServiceImpl implements MovementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovementServiceImpl.class);
    private final TransportUnitApi transportUnitApi;
    private final BeanMapper mapper;
    private final Validator validator;
    private final MovementTypeResolver movementTypeResolver;
    private final Map<MovementType, MovementHandler> handlers;

    MovementServiceImpl(TransportUnitApi transportUnitApi, List<MovementHandler> handlersList, BeanMapper mapper, Validator validator,
            MovementTypeResolver movementTypeResolver) {
        this.transportUnitApi = transportUnitApi;
        this.handlers = handlersList.stream().collect(Collectors.toMap(MovementHandler::getType, h -> h));
        this.mapper = mapper;
        this.validator = validator;
        this.movementTypeResolver = movementTypeResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public MovementVO create(@NotEmpty String bk, @NotNull MovementVO vo) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create a Movement for [{}] with data [{}]", bk, vo);
        }
        resolveType(vo);
        validate(validator, vo, ValidationGroups.Movement.Create.class);
        TransportUnitVO transportUnit = transportUnitApi.findTransportUnit(bk);
        if (transportUnit == null) {
            throw new NotFoundException(format("TransportUnit with BK [%s] does not exist", bk));
        }
        Movement movement = mapper.map(vo, Movement.class);
        movement.setTransportUnitBk(Barcode.of(bk));
        validate(validator, movement, ValidationGroups.Movement.Create.class);
        Movement result = handlers.get(vo.getType()).create(movement);
        return mapper.map(result, MovementVO.class);
    }

    private void resolveType(MovementVO vo) {
        if (vo.getType() == null) {
            if (!vo.hasTarget()) {
                throw new IllegalArgumentException("Can't resolve a MovementType automatically because no target is set");
            }
            vo.setType(movementTypeResolver.resolve(vo.getTransportUnitBk(), vo.getTarget()).orElseThrow(() -> new IllegalArgumentException("Can't resolve MovementType from target")));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Measured
    @Override
    public List<MovementVO> findFor(@NotEmpty String state, @NotNull MovementType... types) {
        return mapper.map(Arrays.stream(types)
                .parallel()
                .map(t -> handlers.get(t).findInState(state))
                .reduce(new ArrayList<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                }), MovementVO.class);
    }
}
