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
package org.openwms.tms.impl;

/**
 * A MovementType.
 *
 * @author Heiko Scherrer
 */
public enum MovementType {

    /** Some arbitrary kind of movement, not defined elsewhere. */
    UNDEFINED,

    /** Manual order movement. */
    MANUAL,

    /** Movement based on a stock check procedure. */
    STOCK_CHECK,

    /** Movement order to clearing. */
    CLEARING,

    /** Movement order for an empty {@literal TransportUnit}. */
    EMPTIES,

    /** Movement for replenishment. */
    REPLENISHMENT,

    /** Movement for inventory control procedure. */
    INVENTORY
}
