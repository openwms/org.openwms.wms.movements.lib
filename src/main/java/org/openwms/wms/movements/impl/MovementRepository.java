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
package org.openwms.wms.movements.impl;

import org.openwms.common.transport.Barcode;
import org.openwms.wms.movements.api.MovementState;
import org.openwms.wms.movements.api.MovementType;
import org.openwms.wms.movements.spi.DefaultMovementState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * A MovementRepository.
 *
 * @author Heiko Scherrer
 */
@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {

    Optional<Movement> findBypKey(String pKey);

    List<Movement> findByTransportUnitBkAndStateIsNot(Barcode transportUnitBk, DefaultMovementState state);

    @Query("select m from Movement m where m.type = :type and m.state = :state and (m.sourceLocationGroupName in :sources or m.sourceLocation in :sources or null = :sources) order by m.createDt")
    List<Movement> findByTypeAndStateAndSource(
            @Param("type") MovementType type,
            @Param("state") MovementState state,
            @Param("sources") List<String> sources);
}
