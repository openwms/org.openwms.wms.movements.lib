/*
 * Copyright 2005-2025 the original author or authors.
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

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openwms.wms.movements.api.MovementVO;
import org.openwms.wms.movements.commands.MovementMO;

/**
 * A MovementMapper.
 *
 * @author Heiko Scherrer
 */
@Mapper
public abstract class MovementMapper {

    public abstract MovementVO convertToVO(MovementMO mo);

    @Mapping(target = "persistentKey", source = "persistentKey")
    @Mapping(target = "transportUnitBk", source = "transportUnitBk.value")
    @Mapping(target = "priority", expression = "java( convertFrom(eo.getPriority()) )")
    @Mapping(target = "target", source = "targetLocationGroup")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "startMode", source = "mode")
    @Mapping(target = "startedAt", source = "startDate")
    @Mapping(target = "finishedAt", source = "endDate")
    @Mapping(target = "createdAt", expression = "java( java.time.LocalDateTime.now().atZone(java.time.ZoneOffset.UTC) )")
    public abstract MovementVO convertToVO(Movement eo);

    @Mapping(target = "persistentKey", source = "persistentKey")
    @Mapping(target = "transportUnitBk", expression = "java( Barcode.of(vo.getTransportUnitBk()) )")
    @Mapping(target = "priority", expression = "java( convertTo(vo.getPriority()) )")
    @Mapping(target = "targetLocationGroup", source = "target")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "mode", source = "startMode")
    @Mapping(target = "startDate", source = "startedAt")
    @Mapping(target = "endDate", source = "finishedAt")
    public abstract Movement convertTo(MovementVO vo);

    public PriorityLevel convertTo(Integer priority) {
        if (priority == null) {
            return PriorityLevel.LOWEST;
        }
        if (priority < 11) {
            return PriorityLevel.HIGHEST;
        } else if (priority < 21) {
            return PriorityLevel.HIGH;
        } else if (priority < 31) {
            return PriorityLevel.NORMAL;
        } else if (priority < 41) {
            return PriorityLevel.LOW;
        }
        return PriorityLevel.LOWEST;
    }

    public Integer convertFrom(PriorityLevel source) {
        if (source == null) {
            return null;
        }
        return source.getOrder();
    }
}
