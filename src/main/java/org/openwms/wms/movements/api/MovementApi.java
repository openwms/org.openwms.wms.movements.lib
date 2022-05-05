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
package org.openwms.wms.movements.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * A MovementApi is the public outer API that can be used by clients.
 *
 * @author Heiko Scherrer
 */
@FeignClient(name = "movement-service", qualifier = "movementApi", decode404 = true)
public interface MovementApi {

    /** API version. */
    public static final String API_VERSION = "v1";
    /** API root to hit Movements (plural). */
    public static final String API_MOVEMENTS = "/" + API_VERSION + "/movements";

    /**
     * Create a {@code Movement} for a {@code TransportUnit}.
     *
     * @param bk The identifying business key of the TransportUnit
     * @param movement The details of the Movement
     */
    @PostMapping("/v1/transport-units/{bk}/movements")
    MovementVO create(
            @PathVariable("bk") String bk,
            @RequestBody MovementVO movement);

    /**
     * Find and return {@code Movement}s.
     *
     * @param state The state the Movement shall reside in
     * @param types Allowed types of Movements to search for
     * @return A list of all instances, never {@literal null}
     */
    @GetMapping(value = "/v1/movements", params = {"state", "types"})
    List<MovementVO> findForStateAndTypes(
            @RequestParam("state") String state,
            @RequestParam("types") MovementType... types);

    /**
     * Find and return all {@code Movement}s order for {@code TransportUnit}s.
     *
     * @param barcode The business key of the TransportUnit
     * @param types A list of types the Movements must resist in
     * @param states A list of states the Movements must resist in
     * @return A list of all instances, never {@literal null}
     */
    @GetMapping(value = "v1/movements", params = {"barcode", "types", "states"})
    List<MovementVO> findForTuAndTypesAndStates(
            @RequestParam("barcode") String barcode,
            @RequestParam("types") List<String> types,
            @RequestParam("states") List<String> states);
}
