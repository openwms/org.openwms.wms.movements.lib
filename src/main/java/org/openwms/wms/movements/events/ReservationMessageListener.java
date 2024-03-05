/*
 * Copyright 2005-2024 the original author or authors.
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
package org.openwms.wms.movements.events;

import org.ameba.annotation.Measured;
import org.openwms.core.SpringProfiles;
import org.openwms.wms.movements.MovementService;
import org.openwms.wms.movements.api.MovementType;
import org.openwms.wms.movements.api.MovementVO;
import org.openwms.wms.movements.commands.SplitMO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * A ReservationMessageListener.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
public class ReservationMessageListener {

    private final MovementService movementService;

    public ReservationMessageListener(MovementService movementService) {
        this.movementService = movementService;
    }

    @Measured
    @RabbitListener(queues = "${owms.events.shipping.split.queue-name}")
    public void handle(@Payload SplitMO mo, @Header("amqp_receivedRoutingKey") String routingKey) {
        var movement = new MovementVO();
        movement.setTransportUnitBk(mo.getTransportUnitBK());
        movement.setPriority(mo.getPriority());
        movement.setTarget(mo.getTargetName());
        movement.setType(MovementType.RELOCATION);
        movementService.create(mo.getTransportUnitBK(), movement);
    }
}
