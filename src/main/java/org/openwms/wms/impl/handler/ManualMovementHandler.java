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
package org.openwms.wms.impl.handler;

import org.ameba.annotation.TxService;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.StartMode;
import org.openwms.wms.impl.Movement;
import org.openwms.wms.impl.MovementCreated;
import org.openwms.wms.impl.MovementHandler;
import org.openwms.wms.impl.MovementRepository;
import org.springframework.context.ApplicationEventPublisher;

/**
 * A ManualMovementHandler.
 *
 * @author Heiko Scherrer
 */
@TxService
class ManualMovementHandler implements MovementHandler {

    private final MovementRepository repository;
    private final ApplicationEventPublisher publisher;

    ManualMovementHandler(MovementRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovementType getType() {
        return MovementType.MANUAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Movement create(Movement movement) {
        movement.setMode(StartMode.AUTOMATIC);
        Movement saved = repository.save(movement);
        publisher.publishEvent(new MovementCreated(saved));
        return saved;
    }
}
