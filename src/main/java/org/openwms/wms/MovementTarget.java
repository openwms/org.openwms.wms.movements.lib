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
package org.openwms.wms;

import org.openwms.wms.api.MovementType;

import java.util.List;

/**
 * A MovementTarget.
 *
 * @author Heiko Scherrer
 */
public class MovementTarget {

    /** The name of the target. */
    private String name;
    /** A comma separated list of LocationGroup names to search the final target in. */
    private List<String> searchLocationGroupNames;
    /** The type of Movement that is assigned. */
    private MovementType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSearchLocationGroupNames() {
        return searchLocationGroupNames;
    }

    public void setSearchLocationGroupNames(List<String> searchLocationGroupNames) {
        this.searchLocationGroupNames = searchLocationGroupNames;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MovementTarget{" +
                "name='" + name + '\'' +
                ", searchLocationGroupNames=" + searchLocationGroupNames +
                ", type=" + type +
                '}';
    }
}
