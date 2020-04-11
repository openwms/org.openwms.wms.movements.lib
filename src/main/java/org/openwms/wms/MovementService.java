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
package org.openwms.wms;

import org.openwms.wms.api.MovementVO;

/**
 * A MovementService.
 *
 * @author Heiko Scherrer
 */
public interface MovementService {

    /**
     * Create a new {@code Movement} for a {@code TransportUnit}.
     *
     * @param bk The identifying business key of the TransportUnit to move
     * @param movement Detailed Movement information
     * @return The created Movement instance
     */
    MovementVO create(String bk, MovementVO movement);
}
