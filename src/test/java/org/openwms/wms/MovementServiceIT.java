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

import org.ameba.exception.BusinessRuntimeException;
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
import org.openwms.wms.api.StartMode;
import org.openwms.wms.impl.Movement;
import org.openwms.wms.movements.spi.common.putaway.PutawayApi;
import org.openwms.wms.spi.DefaultMovementState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import javax.persistence.EntityManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * A MovementServiceIT.
 *
 * @author Heiko Scherrer
 */
@MovementsApplicationTest
class MovementServiceIT {

    @Autowired
    private EntityManager em;
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
                .hasMessageMatching("create.[a-zA-Z0-9]*: must not be empty.*");
        assertThatThrownBy(() -> testee.create(null, inboundMove))
                .isInstanceOf(ServiceLayerException.class)
                .hasMessageMatching("create.[a-zA-Z0-9]*: must not be empty.*");
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
                .hasMessageMatching("create.[a-zA-Z0-9]*: must not be null.*");
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
        //arrange
        LocationVO sourceLocation = new LocationVO("PASS/PASS/PASS/PASS/PASS");
        sourceLocation.setErpCode("PASS");
        given(transportUnitApi.findTransportUnit("4711")).willReturn(new TransportUnitVO("4711"));
        given(locationApi.findLocationByCoordinate("PASS/PASS/PASS/PASS/PASS")).willReturn(Optional.of(sourceLocation));
        var mov = new MovementVO();
        mov.setType(MovementType.INBOUND);
        mov.setSourceLocation("PASS/PASS/PASS/PASS/PASS");
        mov.setTarget("KNOWN");

        // act
        MovementVO result = testee.create("4711", mov);

        // assert
        assertThat(result.getPersistentKey()).isNotEmpty();
        assertThat(result.getSourceLocation()).isEqualTo("PASS");
        assertThat(result.getTarget()).isEqualTo("KNOWN");
        assertThat(result.getType()).isEqualTo(MovementType.INBOUND);
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test
    void test_findFor() {
        var result = testee.findFor(DefaultMovementState.INACTIVE, "HRL.10.20.2.0", MovementType.INBOUND);
        assertThat(result).hasSize(1);
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test
    void test_findAll() {
        var result = testee.findAll();
        assertThat(result).hasSize(3);
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test
    void test_findForTU() {
        var result = testee.findForTU("4712");
        assertThat(result).hasSize(1);
        result = testee.findForTU("4713");
        assertThat(result).isEmpty();
    }

    @Test
    void test_getPriorityList() {
        var list = testee.getPriorityList();
        assertThat(list).hasSize(5);
    }

    @Test
    void test_move_fails_without_state() {
        // arrange
        var vo = MovementVO.builder()
                .build();

        // act & assert
        assertThatThrownBy(() -> testee.move("1000", vo))
                .isInstanceOf(ServiceLayerException.class)
                .hasMessageContaining("state: must not be empty");

        var vo2 = MovementVO.builder()
                .state("INACTIVE")
                .build();

        // act & assert
        assertThatThrownBy(() -> testee.move("1000", vo2))
                .isInstanceOf(ServiceLayerException.class)
                .hasMessageContaining("sourceLocation: must not be empty");

        var vo3 = MovementVO.builder()
                .sourceLocation("PASS/PASS/PASS/PASS/PASS")
                .state("INACTIVE")
                .build();

        // act & assert
        assertThatThrownBy(() -> testee.move("1000", vo3))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Location with locationId [PASS/PASS/PASS/PASS/PASS] does not exist");

        LocationVO sourceLocation = new LocationVO("PASS/PASS/PASS/PASS/PASS");
        sourceLocation.setErpCode("PASS");
        sourceLocation.setLocationGroupName("LG");
        given(locationApi.findLocationByCoordinate("PASS/PASS/PASS/PASS/PASS")).willReturn(Optional.of(sourceLocation));
        // act & assert
        assertThatThrownBy(() -> testee.move("1002", vo3))
                .isInstanceOf(BusinessRuntimeException.class)
                .hasMessageContaining("Movement [1002] cant be moved it is already completed");
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test
    void test_move() {
        // arrange
        LocationVO sourceLocation = new LocationVO("PASS/PASS/PASS/PASS/PASS");
        sourceLocation.setErpCode("PASS");
        sourceLocation.setLocationGroupName("LG");
        given(locationApi.findLocationByCoordinate("PASS/PASS/PASS/PASS/PASS")).willReturn(Optional.of(sourceLocation));
        var vo = MovementVO.builder()
                .sourceLocation("PASS/PASS/PASS/PASS/PASS")
                .state("INACTIVE")
                .build();

        // act
        var moved = testee.move("1000", vo);

        // assert
        assertThat(moved.getPersistentKey()).isEqualTo("1000");
        assertThat(moved.getTransportUnitBk()).isEqualTo("4711");
        assertThat(moved.getStartMode()).isEqualTo(StartMode.MANUAL);
        assertThat(moved.getPriority()).isEqualTo(30);
        assertThat(moved.getState()).isEqualTo("INACTIVE");
        assertThat(moved.getType()).isEqualTo(MovementType.INBOUND);
        assertThat(moved.getSourceLocation()).isEqualTo("PASS");
        assertThat(moved.getSourceLocationGroupName()).isEqualTo("LG");
        assertThat(moved.getTarget()).isEqualTo("WA_01");

        var entity = em.find(Movement.class, 1000L);
        assertThat(entity.getSourceLocation()).isEqualTo("PASS");
        assertThat(entity.getSourceLocationGroupName()).isEqualTo("LG");
    }

    @Test
    void test_complete_fails_without_target() {
        // arrange
        var vo = MovementVO.builder()
                .build();

        // act & assert
        assertThatThrownBy(() -> testee.complete("1002", vo))
                .isInstanceOf(ServiceLayerException.class)
                .hasMessageContaining("target: must not be empty");
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test
    void test_complete() {
        // arrange
        LocationVO sourceLocation = new LocationVO("PASS/PASS/PASS/PASS/PASS");
        sourceLocation.setErpCode("ERPCODE");
        sourceLocation.setLocationGroupName("LG");
        given(locationApi.findLocationByErpCode("ERPCODE")).willReturn(Optional.of(sourceLocation));
        var vo = MovementVO.builder()
                .target("ERPCODE")
                .build();

        // act
        var moved = testee.complete("1000", vo);

        // assert
        assertThat(moved.getPersistentKey()).isEqualTo("1000");
        assertThat(moved.getTransportUnitBk()).isEqualTo("4711");
        assertThat(moved.getStartMode()).isEqualTo(StartMode.MANUAL);
        assertThat(moved.getPriority()).isEqualTo(30);
        assertThat(moved.getState()).isEqualTo("DONE");
        assertThat(moved.getType()).isEqualTo(MovementType.INBOUND);
        assertThat(moved.getSourceLocation()).isEqualTo("HRL.10.20.2.0");
        assertThat(moved.getSourceLocationGroupName()).isEqualTo("STOCK");
        assertThat(moved.getTarget()).isEqualTo("ERPCODE");

        var entity = em.find(Movement.class, 1000L);
        assertThat(entity.getTargetLocation()).isEqualTo("ERPCODE");
        assertThat(entity.getTargetLocationGroup()).isEqualTo("ERPCODE");
    }
}
