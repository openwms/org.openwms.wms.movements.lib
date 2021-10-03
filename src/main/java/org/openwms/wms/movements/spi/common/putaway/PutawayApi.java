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
package org.openwms.wms.movements.spi.common.putaway;

import org.openwms.common.location.api.LocationVO;
import org.openwms.common.putaway.api.PutawayConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * A PutawayApi.
 *
 * @author Heiko Scherrer
 */
@FeignClient(name = "wms-inventory", qualifier = "putawayApi", decode404 = true)
public interface PutawayApi {

    /**
     * Find the next available Location for Putaway in the given {@code LocationGroup}s .
     *
     * @param locationGroupNames Name of the LocationGroups to search a Location for. The ordering is relevant, LG first in the list are
     * preferred to those later in the list
     * @param transportUnitBK The TransportUnit barcode to search a Location for
     * @param sku The SKU of the Product
     * @return Assigned Location for Putaway
     * @throws org.ameba.exception.NotFoundException May throw in case no TransportUnit exists
     */
    @PostMapping(value = PutawayConstants.API_LOCATION_GROUPS,
            params = {"locationGroupNames", "transportUnitBK", "numberOfExpectedTransportUnits"},
            produces = "application/vnd.openwms.location.single-v1+json")
    LocationVO findAndAssignNextInLocGroup(
            @RequestParam("locationGroupNames") List<String> locationGroupNames,
            @RequestParam("transportUnitBK") String transportUnitBK,
            @RequestParam("numberOfExpectedTransportUnits") int numberOfExpectedTransportUnits
    );
}
