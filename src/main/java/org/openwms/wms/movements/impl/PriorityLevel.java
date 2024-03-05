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
package org.openwms.wms.movements.impl;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A PriorityLevel is used to prioritize {@code Movement}s.
 * 
 * @author Heiko Scherrer
 */
public enum PriorityLevel implements Serializable {

    /** Highest priority. */
    HIGHEST(10),

    /** High priority. */
    HIGH(20),

    /** Standard priority. */
    NORMAL(30),

    /** Low priority. */
    LOW(40),

    /** Lowest priority. */
    LOWEST(50);

    private int order;

    /** Dear JPA... */
    PriorityLevel() {
    }

    public static PriorityLevel of(String priority) {
        return Arrays.stream(PriorityLevel.values())
                .filter(p -> p.name().equals(priority))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("A priority level of %s is not defined", priority)));
    }

    /**
     * Initializing constructor.
     *
     * @param order The order
     */
    PriorityLevel(int order) {
        this.order = order;
    }

    /**
     * Get the order.
     * 
     * @return the order.
     */
    public int getOrder() {
        return order;
    }

}
