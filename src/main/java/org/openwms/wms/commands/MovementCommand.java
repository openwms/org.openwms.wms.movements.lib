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
package org.openwms.wms.commands;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A MovementCommand.
 *
 * @author Heiko Scherrer
 */
public class MovementCommand implements Serializable {

    @NotNull
    private final Type type;
    @NotNull
    private final MovementMO movement;

    public MovementCommand(Type type, MovementMO movement) {
        this.type = type;
        this.movement = movement;
    }

    public enum Type {
        CREATE
    }

    public Type getType() {
        return type;
    }

    public MovementMO getMovement() {
        return movement;
    }
}
