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
package org.openwms.wms.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openwms.wms.impl.ValidationGroups;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * A MovementVO encapsulates details about the actual request to move a TransportUnit to a given target.
 *
 * @author Heiko Scherrer
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MovementVO {

    /** The persistent key is returned from the service as soon as the resource has been created. */
    private String persistentKey;
    /** The business key of the TransportUnit is returned from the service. */
    private String transportUnitBk;
    /** The type of Movement must be passed by the caller. */
    @NotNull//(groups = ValidationGroups.Movement.Create.class)
    private MovementType type;
    /** The target where to move the TransportUnit to must be passed by the caller. */
    @NotEmpty(groups = ValidationGroups.Movement.Create.class)
    private String target;
}
