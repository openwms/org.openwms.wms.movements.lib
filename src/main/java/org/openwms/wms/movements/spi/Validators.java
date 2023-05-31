/*
 * Copyright 2005-2023 the original author or authors.
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
package org.openwms.wms.movements.spi;

import org.openwms.wms.movements.impl.Movement;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * A Validators.
 *
 * @author Heiko Scherrer
 */
public interface Validators {

    /**
     * An {@code existingMovement} to the new {@code location} must be validated if it is allowed to be moved.
     *
     * @param existingMovement The persisted instance of the Movement
     * @param location The location to examine for capability to take this Movement
     * @param movement Contains all the data where to move to
     * @return The updated Movement information
     * @throws org.ameba.exception.BusinessRuntimeException in case moving it is not allowed
     */
    default Movement onMove(
            final @NotNull Movement existingMovement,
            final @NotBlank String location,
            final @NotNull Movement movement) {
        return existingMovement;
    }
}
