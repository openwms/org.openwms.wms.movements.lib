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

import org.openwms.core.SpringProfiles;
import org.openwms.wms.events.api.MovementEvent;
import org.openwms.wms.commands.MovementMO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * A MovementEventPropagator is active with the {@value SpringProfiles#ASYNCHRONOUS_PROFILE} profile and propagates internal events to the
 * outer world over AMQP protocol.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
class MovementEventPropagator {

    private final String exchangeName;
    private final AmqpTemplate amqpTemplate;

    MovementEventPropagator(
            @Value("${owms.movements.exchange-name}") String exchangeName,
            AmqpTemplate amqpTemplate) {
        this.exchangeName = exchangeName;
        this.amqpTemplate = amqpTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(MovementCreated event) {
        Movement movement = event.getMovement();
        amqpTemplate.convertAndSend(exchangeName, "movement.event.created",
                MovementEvent.newBuilder()
                        .type(MovementEvent.Type.CREATED)
                        .movement(MovementMO.newBuilder()
                                .transportUnitBK(movement.getTransportUnitBk().getValue())
                                .target(movement.getTargetLocationGroup())
                                .build()
                        )
                        .build()
        );
    }
}
