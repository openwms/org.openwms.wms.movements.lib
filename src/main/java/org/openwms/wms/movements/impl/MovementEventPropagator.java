/*
 * Copyright 2005-2026 the original author or authors.
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
import org.openwms.core.SpringProfiles;
import org.openwms.wms.movements.commands.MovementMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.openwms.wms.movements.events.api.MovementEvent.Type.CANCELLED;
import static org.openwms.wms.movements.events.api.MovementEvent.Type.COMPLETED;
import static org.openwms.wms.movements.events.api.MovementEvent.Type.CREATED;
import static org.openwms.wms.movements.events.api.MovementEvent.Type.MOVED;

/**
 * A MovementEventPropagator is active with the {@value SpringProfiles#ASYNCHRONOUS_PROFILE} profile and propagates internal events to the
 * outer world over AMQP protocol.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
class MovementEventPropagator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MovementEventPropagator.class);
    private final String exchangeName;
    private final AmqpTemplate amqpTemplate;

    MovementEventPropagator(
            @Value("${owms.movements.exchange-name}") String exchangeName,
            AmqpTemplate amqpTemplate) {
        this.exchangeName = exchangeName;
        this.amqpTemplate = amqpTemplate;
    }

    @Measured
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEvent(MovementEvent event) {
        var movement = event.getSource();
        var vo = MovementMO.newBuilder()
                .pKey(movement.getPersistentKey())
                .transportUnitBK(movement.getTransportUnitBk().getValue())
                .initiator(movement.getInitiator())
                .target(movement.getTargetLocationGroup())
                .build();
        switch (event.getType()) {
            case CREATED:
                LOGGER.info("Movement has been CREATED [{}]", movement);
                amqpTemplate.convertAndSend(exchangeName, "movement.event.created",
                        org.openwms.wms.movements.events.api.MovementEvent.newBuilder()
                                .type(CREATED)
                                .movement(vo)
                                .build()
                );
                break;
            case CANCELLED:
                LOGGER.info("Movement has been CANCELLED [{}]", movement);
                amqpTemplate.convertAndSend(exchangeName, "movement.event.cancelled",
                        org.openwms.wms.movements.events.api.MovementEvent.newBuilder()
                                .type(CANCELLED)
                                .movement(vo)
                                .build()
                );
                break;
            case COMPLETED:
                LOGGER.info("Movement has been COMPLETED [{}]", movement);
                amqpTemplate.convertAndSend(exchangeName, "movement.event.completed",
                        org.openwms.wms.movements.events.api.MovementEvent.newBuilder()
                                .type(COMPLETED)
                                .movement(vo)
                                .build()
                );
                break;
            case MOVED:
                LOGGER.info("Movement has been MOVED [{}]", movement);
                amqpTemplate.convertAndSend(exchangeName, "movement.event.moved",
                        org.openwms.wms.movements.events.api.MovementEvent.newBuilder()
                                .type(MOVED)
                                .movement(vo)
                                .build()
                );
                break;
            default:
                LOGGER.warn("MovementEvent of type [{}] is currently not supported", event.getType());
        }
    }
}
