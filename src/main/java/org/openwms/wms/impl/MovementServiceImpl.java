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

import org.ameba.annotation.TxService;
import org.ameba.exception.NotFoundException;
import org.ameba.mapping.BeanMapper;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.wms.MovementService;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.MovementVO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A MovementServiceImpl.
 *
 * @author Heiko Scherrer
 */
@TxService
class MovementServiceImpl implements MovementService {

    private final TransportUnitApi transportUnitApi;
    private final BeanMapper mapper;
    private final Map<MovementType, MovementHandler> handlers;

    MovementServiceImpl(TransportUnitApi transportUnitApi, List<MovementHandler> handlersList, BeanMapper mapper) {
        this.transportUnitApi = transportUnitApi;
        this.handlers = handlersList.stream().collect(Collectors.toMap(MovementHandler::getType, h -> h));
        this.mapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovementVO create(String bk, MovementVO vo) {
        TransportUnitVO transportUnit = transportUnitApi.findTransportUnit(bk);
        if (transportUnit == null) {
            throw new NotFoundException(format("TransportUnit with BK [%s] does not exist", bk));
        }
        Movement movement = mapper.map(vo, Movement.class);
        Movement result = handlers.get(vo.getType()).create(movement);
        return mapper.map(result, MovementVO.class);
    }
}
