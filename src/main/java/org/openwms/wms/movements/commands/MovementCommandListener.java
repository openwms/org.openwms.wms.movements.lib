/*
 * Copyright 2005-2025 the original author or authors.
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
package org.openwms.wms.movements.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.openwms.core.SpringProfiles;
import org.openwms.wms.movements.MovementService;
import org.openwms.wms.movements.impl.MovementMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.validation.annotation.Validated;

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
    private final MovementMapper movementMapper;
    private final MovementService movementService;

    MovementCommandListener(MovementMapper movementMapper, MovementService movementService) {
        this.movementMapper = movementMapper;
        this.movementService = movementService;
    }

    @Measured
    @RabbitListener(queues = "${owms.commands.movements.movement.queue-name}")
    public void onCommand(@Valid @NotNull @Payload MovementCommand command) {
        if (command.getType() == MovementCommand.Type.CREATE) {
            var movement = command.getMovement();
            LOGGER.debug("Got command to create a Movement [{}]", movement);
            movementService.create(movement.getTransportUnitBK(), movementMapper.convertToVO(movement));
        }
    }
}
