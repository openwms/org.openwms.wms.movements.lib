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
package org.openwms.wms.movements.impl;

import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.openwms.wms.movements.Message;
import org.openwms.wms.movements.MovementProperties;
import org.openwms.wms.movements.spi.common.putaway.PutawayApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * A PutawayAdapter is a Spring managed transactional event listener that acts as an adapter to the {@link PutawayApi} that is called after
 * a {@link Movement} has been created successfully.
 *
 * @author Heiko Scherrer
 */
@TxService
@RefreshScope
class PutawayAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PutawayAdapter.class);
    private final ApplicationEventPublisher eventPublisher;
    private final MovementProperties properties;
    private final MovementRepository repository;
    private final PutawayApi putawayApi;

    PutawayAdapter(ApplicationEventPublisher eventPublisher, MovementProperties properties, MovementRepository repository, PutawayApi putawayApi) {
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.repository = repository;
        this.putawayApi = putawayApi;
    }

    @ConditionalOnExpression("${owms.movement.putaway-resolution-enabled}")
    @Measured
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {Exception.class})
    public void onEvent(MovementEvent event) {
        var movement = event.getSource();
        if (event.getType() == MovementEvent.Type.CREATED) {
            if (movement.emptyTargetLocation()) {
                try {
                    var movementTarget = properties.findTarget(movement.getTargetLocationGroup());
                    LOGGER.debug("Call putaway strategy to find target location for movement [{}] in [{}]", movement.getPersistentKey(), movementTarget.getSearchLocationGroupNames());
                    var target = putawayApi.findAndAssignNextInLocGroup(
                            movement.getInitiator(),
                            movementTarget.getSearchLocationGroupNames(),
                            movement.getTransportUnitBk().getValue(),
                            2
                    );
                    LOGGER.debug("Putaway strategy returned [{}] as next target for movement [{}]", target.getLocationId(), movement.getPersistentKey());
                    movement.setTargetLocation(target.getErpCode());
                    if (target.getErpCode() != null) {
                        eventPublisher.publishEvent(new MovementTargetChangedEvent(movement));
                    }
                } catch (Exception e) {
                    LOGGER.error("Error calling the putaway strategy: " + e.getMessage(), e);
                    movement.addProblem(new ProblemHistory(movement, new Message.Builder().withMessageText(e.getMessage()).build()));
                }
                repository.save(movement);
            } else {
                LOGGER.debug("Target is already set and not being resolved");
            }
        }
    }
}
