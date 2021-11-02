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

import org.ameba.annotation.TxService;
import org.ameba.exception.NotFoundException;
import org.openwms.common.location.api.LocationVO;
import org.openwms.wms.Message;
import org.openwms.wms.MovementProperties;
import org.openwms.wms.MovementTarget;
import org.openwms.wms.movements.spi.common.putaway.PutawayApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static java.lang.String.format;

/**
 * A PutawayAdapter is a Spring managed transactional event listener that acts as an adapter to the {@link PutawayApi} that is called after
 * a {@link Movement} has been created successfully.
 *
 * @author Heiko Scherrer
 */
@TxService
class PutawayAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PutawayAdapter.class);
    private final MovementProperties properties;
    private final MovementRepository repository;
    private final PutawayApi putawayApi;

    PutawayAdapter(MovementProperties properties, MovementRepository repository, PutawayApi putawayApi) {
        this.properties = properties;
        this.repository = repository;
        this.putawayApi = putawayApi;
    }

    @ConditionalOnExpression("${owms.movement.putaway-resolution-enabled}")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {Exception.class})
    public void onEvent(MovementCreated event) {
        if (event.getMovement().emptyTargetLocation()) {
            Movement movement = repository.findById(event.getMovement().getPk()).orElseThrow(() -> new NotFoundException(format("Movement with PK [%d] does not exist", event.getMovement().getPk())));
            try {
                MovementTarget movementTarget = properties.findTarget(event.getMovement().getTargetLocationGroup());
                LOGGER.debug("Call putaway strategy to find target location for movement [{}] in [{}]", event.getMovement().getPersistentKey(), movementTarget.getSearchLocationGroupNames());
                LocationVO target = putawayApi.findAndAssignNextInLocGroup(
                        movementTarget.getSearchLocationGroupNames(),
                        event.getMovement().getTransportUnitBk().getValue(),
                        2
                );
                LOGGER.debug("Putaway strategy returned [{}] as next target for movement [{}]", target.getLocationId(), event.getMovement().getPersistentKey());
                movement.setTargetLocation(target.getErpCode());
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
