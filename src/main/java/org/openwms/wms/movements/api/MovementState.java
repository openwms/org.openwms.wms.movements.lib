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
package org.openwms.wms.movements.api;

/**
 * A MovementState is used to drive and track the execution of {@code Movement}s and {@code MovementGroup}s and meant to be extended in
 * projects.
 *
 * @author Heiko Scherrer
 */
public interface MovementState {

    /**
     * Get the name of the state.
     *
     * @return The name of the Movement
     */
    String getName();
}
