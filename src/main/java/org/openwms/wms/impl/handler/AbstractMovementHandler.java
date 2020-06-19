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

import org.ameba.exception.NotFoundException;
import org.openwms.common.location.LocationPK;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.impl.Location;
import org.openwms.wms.impl.LocationRepository;
import org.openwms.wms.impl.Movement;
import org.openwms.wms.impl.MovementCreated;
import org.openwms.wms.impl.MovementHandler;
import org.openwms.wms.impl.MovementRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static java.lang.String.format;

/**
 * A AbstractMovementHandler.
 *
 * @author Heiko Scherrer
 */
abstract class AbstractMovementHandler implements MovementHandler {

    protected final MovementRepository repository;
    protected final LocationRepository locationRepository;
    protected final ApplicationEventPublisher publisher;

    AbstractMovementHandler(MovementRepository repository, LocationRepository locationRepository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.locationRepository = locationRepository;
        this.publisher = publisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Movement create(Movement movement) {
        Location targetLocation = movement.getTargetLocation();
        if (targetLocation != null && targetLocation.isNew()) {
            LocationPK loc = targetLocation.getLocationId();
            targetLocation = locationRepository.findByLocationId(targetLocation.getLocationId()).orElseThrow(
                    () -> new NotFoundException(format("No Location with ID [%s] exists", loc.toString()))
            );
            movement.setTargetLocation(targetLocation);
        }
        Movement saved = repository.save(movement);
        publisher.publishEvent(new MovementCreated(saved));
        return saved;
    }

    protected List<Movement> findInState(String state, MovementType type ) {
        if ("ACTIVE".equals(state)) {
            return repository.findActiveOnes(type);
        } else if ("INACTIVE".equals(state)) {
            return repository.findInactiveOnes(type);
        } else {
            throw new IllegalArgumentException(format("The given state is not recognized [%s]", state));
        }
    }
}
