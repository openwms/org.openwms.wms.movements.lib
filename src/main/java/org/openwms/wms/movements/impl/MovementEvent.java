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
package org.openwms.wms.movements.impl;

import org.openwms.core.event.RootApplicationEvent;

/**
 * A MovementEvent signals changes on a {@link Movement}s lifecycle.
 *
 * @author Heiko Scherrer
 */
public class MovementEvent extends RootApplicationEvent {

    private final Type type;

    public enum Type {
        CREATED, CANCELLED, COMPLETED, MOVED
    }

    public MovementEvent(Movement movement, Type type) {
        super(movement);
        if (type == null) {
            throw new IllegalArgumentException("null type");
        }
        this.type = type;
    }

    @Override
    public Movement getSource() {
        return (Movement) super.getSource();
    }

    public Type getType() {
        return type;
    }
}
