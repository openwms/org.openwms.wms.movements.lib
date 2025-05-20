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
package org.openwms.wms.movements.impl.handler;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.ameba.annotation.Measured;
import org.ameba.annotation.TxService;
import org.openwms.wms.movements.api.MovementState;
import org.openwms.wms.movements.api.MovementType;
import org.openwms.wms.movements.impl.Movement;
import org.openwms.wms.movements.impl.MovementRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * A ManualMovementHandler.
 *
 * @author Heiko Scherrer
 */
@TxService
class ManualMovementHandler extends AbstractMovementHandler {

    ManualMovementHandler(MovementRepository repository, ApplicationEventPublisher publisher) {
        super(repository, publisher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public List<Movement> findInStateAndSource(@NotNull MovementState state, @NotEmpty List<String> sources) {
        return findInState(state, sources, MovementType.MANUAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(MovementType delimiter) {
        return delimiter == MovementType.MANUAL;
    }
}
