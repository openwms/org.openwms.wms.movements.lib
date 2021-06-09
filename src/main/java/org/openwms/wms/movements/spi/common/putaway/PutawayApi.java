/*
 * Copyright 2005-2019 the original author or authors.
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
package org.openwms.wms.movements.spi.common.putaway;

import org.openwms.common.location.api.LocationVO;
import org.openwms.common.putaway.api.PutawayConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * A PutawayApi.
 *
 * @author Heiko Scherrer
 */
@FeignClient(name = "common-service", qualifier = "putawayApi", decode404 = true)
public interface PutawayApi {

    /**
     * Find proper stock locations for infeed in the {@code LocationGroup} identified by the {@code locationGroupNames}.
     *
     * @param locationGroupNames Names of the LocationGroups to search Locations for
     * @param transportUnitBK The unique (physical) identifier of the TransportUnit to search a Location for
     * @return Next free Location for infeed
     * @throws org.ameba.exception.NotFoundException May throw in case no Location available
     */
    @GetMapping(
            value = PutawayConstants.API_LOCATION_GROUPS,
            params = {"locationGroupNames", "transportUnitBK"}
    )
    List<LocationVO> findInAisle(
            @RequestParam("locationGroupNames") String locationGroupNames,
            @RequestParam("transportUnitBK") String transportUnitBK
    );

    /**
     * Find the next stock location for infeed in the {@code LocationGroup} identified by the {@code locationGroupNames}.
     *
     * @param locationGroupNames Names of the LocationGroups to search a Location for
     * @param transportUnitBK The unique (physical) identifier of the TransportUnit to search a Location for
     * @return Next free Location for infeed
     * @throws org.ameba.exception.NotFoundException May throw in case no Location available
     */
    @GetMapping(
            value = PutawayConstants.API_LOCATION_GROUPS,
            params = {"locationGroupNames", "transportUnitBK"},
            produces = "application/vnd.openwms.location.single-v1+json"
    )
    LocationVO findNextInAisle(
            @RequestParam("locationGroupNames") List<String> locationGroupNames,
            @RequestParam("transportUnitBK") String transportUnitBK
    );

    /**
     * Find the next available location for infeed in the {@code LocationGroup} identified by the {@code locationGroupNames} and assign the
     * {@code TransportUnit} with the given business key to this target.
     *
     * @param locationGroupNames Names of the LocationGroups to search a Location for
     * @param transportUnitBK The unique (physical) identifier of the TransportUnit to search a Location for
     * @return Assigned Location for infeed
     * @throws org.ameba.exception.NotFoundException May throw in case no Location available
     */
    @PostMapping(
            value = PutawayConstants.API_LOCATION_GROUPS,
            params = {"locationGroupNames", "transportUnitBK"},
            produces = "application/vnd.openwms.location.single-v1+json"
    )
    LocationVO findAndAssignNextInLocGroup(
            @RequestParam("locationGroupNames") List<String> locationGroupNames,
            @RequestParam("transportUnitBK") String transportUnitBK
    );
}
