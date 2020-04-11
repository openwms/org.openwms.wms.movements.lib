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

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * A MovementsApi.
 *
 * @author Heiko Scherrer
 */
@FeignClient(name = "movement-service", qualifier = "movementApi", decode404 = true)
public interface MovementsApi {

    /**
     * Create a {@code Movement} of a {@code TransportUnit}.
     *
     * @param bk The identifying business key of the TransportUnit
     * @param movement The details of the Movement
     */
    @PostMapping("/v1/transport-units/{bk}/movements")
    void create(@PathVariable("bk") String bk, MovementVO movement);
}
