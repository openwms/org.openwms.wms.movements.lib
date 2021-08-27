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
package org.openwms.wms.commands;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.ameba.mapping.BeanMapper;
import org.openwms.core.SpringProfiles;
import org.openwms.wms.MovementService;
import org.openwms.wms.api.MovementVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

/**
 * A MovementCommandListener.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Validated
@TxService
class MovementCommandListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovementCommandListener.class);
    private final BeanMapper mapper;
    private final MovementService movementService;

    MovementCommandListener(BeanMapper mapper, MovementService movementService) {
        this.mapper = mapper;
        this.movementService = movementService;
    }

    @Measured
    @RabbitListener(queues = "${owms.commands.movements.movement.queue-name}")
    public void onCommand(@Valid @Payload MovementCommand command) {
        switch (command.getType()) {
            case CREATE:
                MovementMO movement = command.getMovement();
                LOGGER.debug("Got command to create a Movement [{}]", movement);
                movementService.create(movement.getTransportUnitBK(), mapper.map(movement, MovementVO.class));
                break;
            default:
        }
    }
}
