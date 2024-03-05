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
package org.openwms.wms.movements.spi;

import org.openwms.wms.movements.impl.Movement;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * A DefaultValidatorsImpl is used to instantiate concrete Spring Beans from the interface.
 *
 * @author Heiko Scherrer
 */
@Validated
@Component
class DefaultValidatorsImpl implements Validators {

    /**
     * {@inheritDoc}
     *
     * Just to satisfy the default interface and have a concrete bean instantiation here.
     */
    @Override
    public Movement onMove(@NotNull Movement existingMovement, @NotBlank String location, @NotNull Movement movement) {
        return Validators.super.onMove(existingMovement, location, movement);
    }
}
