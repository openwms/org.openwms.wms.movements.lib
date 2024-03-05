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
package org.openwms.wms.movements.spi;

import org.openwms.wms.movements.api.MovementType;

import javax.validation.constraints.NotEmpty;
import java.util.Optional;

/**
 * A MovementTypeResolver tries to resolve the type of {@code Movement} from given parameters.
 *
 * @author Heiko Scherrer
 */
public interface MovementTypeResolver {

    /**
     * Resolve the type of {@code Movement}.
     *
     * @param transportUnitBK The current business key of the TransportUnit
     * @param target The current target of the Movement
     * @return The type of Movement
     */
    Optional<MovementType> resolve(@NotEmpty String transportUnitBK, @NotEmpty String target);
}
