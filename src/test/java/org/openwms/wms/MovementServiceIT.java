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
package org.openwms.wms;

import org.ameba.exception.NotFoundException;
import org.ameba.exception.ServiceLayerException;
import org.junit.jupiter.api.Test;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.MovementVO;
import org.openwms.wms.movements.spi.common.putaway.PutawayApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

/**
 * A MovementServiceIT.
 *
 * @author Heiko Scherrer
 */
@MovementsApplicationTest
class MovementServiceIT {

    @MockBean
    protected TransportUnitApi transportUnitApi;
    @MockBean
    protected LocationApi locationApi;
    @MockBean
    protected LocationGroupApi locationGroupApi;
    @MockBean
    protected PutawayApi putawayApi;
    @Autowired
    private MovementService testee;

    @Test
    void test_create_with_empty_Barcode() {
        MovementVO inboundMove = createInvalidMovement();
        assertThatThrownBy(() -> testee.create("", inboundMove))
                .isInstanceOf(ServiceLayerException.class)
                .hasMessageContaining("create.bk: must not be empty");
        assertThatThrownBy(() -> testee.create(null, inboundMove))
                .isInstanceOf(ServiceLayerException.class)
                .hasMessageContaining("create.bk: must not be empty");
    }

    private MovementVO createInvalidMovement() {
        var inboundMove = new MovementVO();
        inboundMove.setType(MovementType.INBOUND);
        inboundMove.setSourceLocation("KNOWN");
        inboundMove.setTarget("KNOWN");
        return inboundMove;
    }

    private MovementVO createValidMovement() {
        var inboundMove = new MovementVO();
        inboundMove.setTransportUnitBk("KNOWN");
        inboundMove.setType(MovementType.INBOUND);
        inboundMove.setSourceLocation("KNOWN");
        inboundMove.setTarget("KNOWN");
        return inboundMove;
    }

    @Test
    void test_create_with_empty_Movement() {
        assertThatThrownBy(() -> testee.create("UNKNOWN", null))
                .isInstanceOf(ServiceLayerException.class)
                .hasMessageContaining("create.vo: must not be null");
    }

    @Test
    void test_create_with_unknown_TU() {
        given(transportUnitApi.findTransportUnit("UNKNOWN")).willThrow(new NotFoundException());
        var mov = createValidMovement();
        assertThatThrownBy(() -> testee.create("UNKNOWN", mov))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("TransportUnit with BK [UNKNOWN] does not exist");
    }

    @Test
    void test_create_with_unknown_SourceLocationId() {
        given(transportUnitApi.findTransportUnit("4711")).willReturn(new TransportUnitVO("4711"));
        given(locationApi.findLocationByCoordinate("UNKN/UNKN/UNKN/UNKN/UNKN")).willThrow(new NotFoundException());
        var mov = new MovementVO();
        mov.setType(MovementType.INBOUND);
        mov.setSourceLocation("UNKN/UNKN/UNKN/UNKN/UNKN");
        mov.setTarget("KNOWN");
        assertThatThrownBy(() -> testee.create("4711", mov))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Location with locationId [UNKN/UNKN/UNKN/UNKN/UNKN] does not exist");
    }

    @Test
    void test_create_with_unknown_SourceErpCode() {
        given(transportUnitApi.findTransportUnit("4711")).willReturn(new TransportUnitVO("4711"));
        given(locationApi.findLocationByErpCode("UNKNOWN")).willThrow(new NotFoundException());
        var mov = new MovementVO();
        mov.setType(MovementType.INBOUND);
        mov.setSourceLocation("UNKNOWN");
        mov.setTarget("KNOWN");
        assertThatThrownBy(() -> testee.create("4711", mov))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Location with erpCode [UNKNOWN] does not exist");
    }

    @Test
    void test_create_with_success() {
        LocationVO sourceLocation = new LocationVO("PASS/PASS/PASS/PASS/PASS");
        sourceLocation.setErpCode("PASS");
        given(transportUnitApi.findTransportUnit("4711")).willReturn(new TransportUnitVO("4711"));
        given(locationApi.findLocationByCoordinate("PASS/PASS/PASS/PASS/PASS"))
                .willReturn(Optional.of(sourceLocation));
        var mov = new MovementVO();
        mov.setType(MovementType.INBOUND);
        mov.setSourceLocation("PASS/PASS/PASS/PASS/PASS");
        mov.setTarget("KNOWN");
        MovementVO result = testee.create("4711", mov);
        assertThat(result.getPersistentKey()).isNotEmpty();
        assertThat(result.getSourceLocation()).isEqualTo("PASS");
        assertThat(result.getTarget()).isEqualTo("KNOWN");
        assertThat(result.getType()).isEqualTo(MovementType.INBOUND);
    }

    @Test
    void findFor() {
    }

    @Test
    void getPriorityList() {
    }

    @Test
    void move() {
    }

    @Test
    void complete() {
    }
}