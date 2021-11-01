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

import org.openwms.wms.api.MovementState;
import org.openwms.wms.api.MovementType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A MovementHandler does all the work according to the specific type of {@link Movement}.
 *
 * @author Heiko Scherrer
 */
public interface MovementHandler {

    /**
     * Return the type the handler implementation can handle.
     *
     * @return The type
     */
    MovementType getType();

    /**
     * Create and return a {@code Movement}.
     *
     * @param movement The Movement to create
     * @return The created instance
     */
    Movement create(@NotNull Movement movement);

    /**
     * Find and return {@code Movement}s.
     *
     * @param state The state to search Movements for
     * @param sources A list of sourceLocation or sourceLocationGroup names to search Movements for
     * @return A list of instances, never {@literal null}
     */
    List<Movement> findInStateAndSource(@NotNull MovementState state, @NotEmpty List<String> sources);
}
