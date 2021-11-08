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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationGroupApi;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.transport.api.TransportUnitApi;
import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.wms.movements.api.MovementVO;
import org.openwms.wms.movements.spi.common.putaway.PutawayApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.openwms.wms.movements.api.MovementApi.API_MOVEMENTS;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A MovementDocumentation.
 *
 * @author Heiko Scherrer
 */
@MovementsApplicationTest
class MovementDocumentation {

    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockBean
    protected TransportUnitApi transportUnitApi;
    @MockBean
    protected LocationApi locationApi;
    @MockBean
    protected LocationGroupApi locationGroupApi;
    @MockBean
    protected PutawayApi putawayApi;

    /**
     * Do something before each test method.
     */
    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation, WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @AfterEach
    public void reset_mocks() {
        Mockito.reset(transportUnitApi);
    }

    @Test void shall_create_a_movement() throws Exception {
        TransportUnitVO transportUnit = TransportUnitVO.newBuilder().barcode("4711").build();
        given(transportUnitApi.findTransportUnit("4711")).willReturn(transportUnit);
        LocationVO sourceLocation = new LocationVO("WE__/0001/0000/0000/0000");
        sourceLocation.setErpCode("WE_01");
        sourceLocation.setLocationGroupName("WE");
        given(locationApi.findLocationByErpCode("WE_01")).willReturn(Optional.of(sourceLocation));

        MovementVO m = new MovementVO();
        m.setTransportUnitBk("4711");
        m.setSourceLocation("WE_01");
        m.setTarget("ERR_/0001/0000/0000/0000");
        mockMvc.perform(
                post("/v1/transport-units/4711/movements")
                        .content(objectMapper.writeValueAsString(m)).contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andDo(document("move-create"))
        ;
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_findAll() throws Exception {
        mockMvc.perform(
                    get(API_MOVEMENTS)
                )
                .andExpect(status().isOk())
                .andDo(document("move-find-all"))
        ;
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_cancel_a_movement() throws Exception {
        TransportUnitVO transportUnit = TransportUnitVO.newBuilder().barcode("4711").build();
        mockMvc.perform(
                        delete(API_MOVEMENTS + "/1000")
                )
                .andExpect(status().isOk())
                .andDo(document("move-cancel"))
        ;
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_move_a_movement() throws Exception {
        TransportUnitVO transportUnit = TransportUnitVO.newBuilder().barcode("4711").build();
        given(transportUnitApi.findTransportUnit("4711")).willReturn(transportUnit);
        LocationVO sourceLocation = new LocationVO("LOC_/0002/0000/0000/0000");
        sourceLocation.setErpCode("LOC2");
        sourceLocation.setLocationGroupName("STOCK");
        given(locationApi.findLocationByErpCode("LOC2")).willReturn(Optional.of(sourceLocation));

        MovementVO m = new MovementVO();
        m.setTransportUnitBk("4711");
        m.setSourceLocation("LOC2");
        m.setState("ACTIVE");
        mockMvc.perform(
                        patch("/v1/movements/1000")
                                .content(objectMapper.writeValueAsString(m)).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("move-move"))
        ;
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_move_a_completed_movement_fails() throws Exception {
        TransportUnitVO transportUnit = TransportUnitVO.newBuilder().barcode("4711").build();
        given(transportUnitApi.findTransportUnit("4711")).willReturn(transportUnit);
        LocationVO sourceLocation = new LocationVO("LOC_/0002/0000/0000/0000");
        sourceLocation.setErpCode("LOC2");
        sourceLocation.setLocationGroupName("STOCK");
        given(locationApi.findLocationByErpCode("LOC2")).willReturn(Optional.of(sourceLocation));

        MovementVO m = new MovementVO();
        m.setTransportUnitBk("4711");
        m.setSourceLocation("LOC2");
        m.setState("ACTIVE");
        mockMvc.perform(
                        patch("/v1/movements/1002")
                                .content(objectMapper.writeValueAsString(m)).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andDo(document("move-move-with-completed"))
        ;
    }
}
