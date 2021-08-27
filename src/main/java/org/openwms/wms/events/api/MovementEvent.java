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
package org.openwms.wms.events.api;

import org.openwms.wms.commands.MovementMO;

import java.io.Serializable;

/**
 * A MovementEvent.
 *
 * @author Heiko Scherrer
 */
public class MovementEvent implements Serializable {

    private Type type;
    private MovementMO movement;

    private MovementEvent(Builder builder) {
        type = builder.type;
        movement = builder.movement;
    }

    public MovementEvent() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public MovementMO getMovement() {
        return movement;
    }

    public void setMovement(MovementMO movement) {
        this.movement = movement;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public enum Type {
        /** Movement has been created. */
        CREATED
    }

    public static final class Builder {
        private Type type;
        private MovementMO movement;

        private Builder() {
        }

        public Builder type(Type val) {
            type = val;
            return this;
        }

        public Builder movement(MovementMO val) {
            movement = val;
            return this;
        }

        public MovementEvent build() {
            return new MovementEvent(this);
        }
    }
}
