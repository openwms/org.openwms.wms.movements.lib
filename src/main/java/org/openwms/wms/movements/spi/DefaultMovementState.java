/*
 * Copyright 2005-2022 the original author or authors.
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

import org.openwms.wms.movements.api.MovementState;

/**
 * A DefaultMovementState defines the states in the default implementation.
 *
 * @author Heiko Scherrer
 * @see MovementState
 */
public enum DefaultMovementState implements MovementState {

    /** Ready to be executed. */
    INACTIVE,

    /** Currently in execution. */
    ACTIVE,

    /** Cancelled before final execution. */
    CANCELLED,

    /** Executed and done. */
    DONE;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name();
    }
}
