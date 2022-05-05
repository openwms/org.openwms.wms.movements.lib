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
package org.openwms.wms.movements.impl;

import org.openwms.core.event.RootApplicationEvent;
import org.springframework.util.Assert;

/**
 * A MovementEvent signals changes on a {@link Movement}s lifecycle.
 *
 * @author Heiko Scherrer
 */
public class MovementEvent extends RootApplicationEvent {

    private final Type type;
    private String previousLocation;

    public enum Type {
        CREATED, CANCELLED, COMPLETED, MOVED
    }

    public MovementEvent(Movement movement, Type type) {
        super(movement);
        Assert.notNull(type, "type must not be null");
        this.type = type;
    }

    public MovementEvent(Movement movement, Type type, String previousLocation) {
        super(movement);
        Assert.notNull(type, "type must not be null");
        this.type = type;
        this.previousLocation = previousLocation;
    }

    @Override
    public Movement getSource() {
        return (Movement) super.getSource();
    }

    public Type getType() {
        return type;
    }

    public String getPreviousLocation() {
        return previousLocation;
    }
}
