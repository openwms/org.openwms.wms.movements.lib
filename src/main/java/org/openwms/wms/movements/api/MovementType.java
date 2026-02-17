/*
 * Copyright 2005-2026 the original author or authors.
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
package org.openwms.wms.movements.api;

/**
 * A MovementType defines all possible types of {@code Movement}s in a project. Each {@code Movement} must be of a specific type.
 *
 * @author Heiko Scherrer
 */
public enum MovementType {

    /** Some arbitrary kind of movement, not defined elsewhere. */
    UNDEFINED,

    /** Arbitrary manual movement. */
    MANUAL,

    /** Movement based on a stock check procedure. */
    STOCK_CHECK,

    /** Movement into stock (hand over from external parties to own system). */
    INBOUND,

    /** Movement out of stock (hand over from own system to external parties). */
    OUTBOUND,

    /** Movement to clearing or error check. */
    CLEARING,

    /** Movement for an empty {@code TransportUnit}. */
    EMPTIES,

    /** Movement for replenishment. */
    REPLENISHMENT,

    /** Movement for reconciliation procedure. */
    RECONCILIATION,

    /** Relocation within a warehouse. */
    RELOCATION,

    /** Relocation between warehouses. */
    WAREHOUSE_RELOCATION
}
