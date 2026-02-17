/*
 * Copyright 2005-2026 the original author or authors.
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
package org.openwms.wms.movements;

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
import org.openwms.transactions.api.commands.AsyncTransactionApi;
import org.openwms.wms.movements.api.MovementVO;
import org.openwms.wms.movements.spi.common.AsyncTransportUnitApi;
import org.openwms.wms.movements.spi.common.putaway.PutawayApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.openwms.wms.movements.api.MovementApi.API_MOVEMENTS;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    @MockitoBean
    protected TransportUnitApi transportUnitApi;
    @MockitoBean
    protected LocationApi locationApi;
    @MockitoBean
    protected LocationGroupApi locationGroupApi;
    @MockitoBean
    protected PutawayApi putawayApi;
    @MockitoBean
    protected AsyncTransactionApi asyncTransactionApi;
    @MockitoBean
    protected AsyncTransportUnitApi asyncTransportUnitApi;

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

    @Test void shall_create_index() throws Exception {
        mockMvc.perform(get("/v1/movements/index"))
                .andDo(document("move-index"))
                .andExpect(status().isOk())
        ;
    }

    @Test void shall_create_a_movement() throws Exception {
        var transportUnit = TransportUnitVO.newBuilder().barcode("4711").build();
        given(transportUnitApi.findTransportUnit("4711")).willReturn(transportUnit);
        var sourceLocation = new LocationVO("WE__/0001/0000/0000/0000");
        sourceLocation.setErpCode("WE_01");
        sourceLocation.setLocationGroupName("WE");
        given(locationApi.findByErpCode("WE_01")).willReturn(Optional.of(sourceLocation));
        var sourceLocationGroupNames = new ArrayList<String>();
        sourceLocationGroupNames.add("CLEARING");
        given(putawayApi.findAndAssignNextInLocGroup("ERP", sourceLocationGroupNames, "4711", 2)).willReturn(sourceLocation);

        var m = new MovementVO();
        m.setInitiator("ERP");
        m.setSourceLocation("WE_01");
        m.setTarget("ERR_/0001/0000/0000/0000");
        mockMvc.perform(
                    post("/v1/transport-units/4711/movements")
                            .content(objectMapper.writeValueAsString(m))
                            .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(document("move-create",
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("initiator").optional().description("(Optional) Initiator of the Movement, who ordered or triggered it"),
                                fieldWithPath("mode").optional().description("(Optional) Whether the Movement should be directly processed (AUTOMATIC) or delayed (MANUAL)"),
                                fieldWithPath("sourceLocation").description("The source Location where the TransportUnit shall be picked up"),
                                fieldWithPath("target").description("The target where to move the TransportUnit to")
                        ),
                        responseFields(
                                fieldWithPath("pKey").description("The persistent technical key of the Movement"),
                                fieldWithPath("transportUnitBk").description("The business key of the TransportUnit to create"),
                                fieldWithPath("type").description("The type of Movement"),
                                fieldWithPath("initiator").description("Initiator of the Movement, who ordered or triggered it"),
                                fieldWithPath("mode").description("Whether the Movement should be directly processed (AUTOMATIC) or delayed (MANUAL)"),
                                fieldWithPath("priority").description("A priority how fast the Movement needs to be processed; A higher value means less prior than lower values"),
                                fieldWithPath("state").description("The current state of the Movement"),
                                fieldWithPath("sourceLocation").description("The source Location where the TransportUnit shall be picked up"),
                                fieldWithPath("sourceLocationGroupName").description("The name of the LocationGroup the sourceLocation belongs to"),
                                fieldWithPath("target").description("The target where to move the TransportUnit to"),
                                fieldWithPath("targetLocationGroup").description("The target LocationGroup used to define in what area"),
                                fieldWithPath("createDt").description("Timestamp when the Movement has been created")
                        )
                ))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
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
    @Test void shall_find_for_TU_Type_State() throws Exception {
        mockMvc.perform(
                        get(API_MOVEMENTS)
                                .param("barcode", "4711")
                                .param("types", "INBOUND")
                                .param("states", "INACTIVE")
                )
                .andDo(document("move-find-tuTypesStates",
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("barcode").description("The business key of the TransportUnit to search for"),
                                parameterWithName("types").description("The Movement types to search for"),
                                parameterWithName("states").description("The Movement states to search for")
                        ),
                        responseFields(
                                fieldWithPath("[].pKey").description("The persistent technical key of the Movement"),
                                fieldWithPath("[].transportUnitBk").description("The business key of the TransportUnit to move"),
                                fieldWithPath("[].type").description("The type of Movement"),
                                fieldWithPath("[].initiator").description("Initiator of the Movement, who ordered or triggered it"),
                                fieldWithPath("[].mode").description("Whether the Movement should be directly processed (AUTOMATIC) or delayed (MANUAL)"),
                                fieldWithPath("[].sku").description("Refers to the demanded Product for that the Movement has been created"),
                                fieldWithPath("[].priority").description("A priority how fast the Movement needs to be processed; A higher value means less prior than lower values"),
                                fieldWithPath("[].state").description("The current state of the Movement"),
                                fieldWithPath("[].sourceLocation").description("The source Location where the TransportUnit shall be picked up"),
                                fieldWithPath("[].sourceLocationGroupName").description("The name of the LocationGroup the sourceLocation belongs to"),
                                fieldWithPath("[].target").description("The target where to move the TransportUnit to"),
                                fieldWithPath("[].targetLocationGroup").description("The target LocationGroup used to define in what area"),
                                fieldWithPath("[].startedAt").description("Timestamp when the Movement has been started"),
                                fieldWithPath("[].latestDueAt").description("Timestamp until when the Movement must be done"),
                                fieldWithPath("[].finishedAt").description("Timestamp when the Movement has been finished"),
                                fieldWithPath("[].createDt").description("Timestamp when the Movement has been created")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].transportUnitBk", is("4711")))
                .andExpect(jsonPath("$.[0].type", is("INBOUND")))
                .andExpect(jsonPath("$.[0].state", is("INACTIVE")))
        ;
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_find_for_State_Types_Source() throws Exception {
        mockMvc.perform(
                        get(API_MOVEMENTS)
                                .param("state", "DONE")
                                .param("types", "INBOUND,REPLENISHMENT")
                                .param("source", "STOCK")
                )
                .andDo(document("move-find-stateTypesSource",
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("state").description("The Movement states to search for"),
                                parameterWithName("types").description("The Movement types to search for"),
                                parameterWithName("source").description("Either the source Location or the name of the source LocationGroup to search Movements for")
                        )
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
        ;
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_cancel_a_movement() throws Exception {
        mockMvc.perform(
                        delete(API_MOVEMENTS + "/1000")
                )
                .andExpect(status().isOk())
                .andDo(document("move-cancel"))
        ;
    }

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_move_a_movement() throws Exception {
        var transportUnit = TransportUnitVO.newBuilder().barcode("4711").build();
        given(transportUnitApi.findTransportUnit("4711")).willReturn(transportUnit);
        var sourceLocation = new LocationVO("LOC_/0002/0000/0000/0000");
        sourceLocation.setErpCode("LOC2");
        sourceLocation.setLocationGroupName("STOCK");
        given(locationApi.findByErpCode("LOC2")).willReturn(Optional.of(sourceLocation));

        var m = new MovementVO();
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
        var transportUnit = TransportUnitVO.newBuilder().barcode("4713").build();
        given(transportUnitApi.findTransportUnit("4713")).willReturn(transportUnit);
        var sourceLocation = new LocationVO("LOC_/0002/0000/0000/0000");
        sourceLocation.setErpCode("LOC2");
        sourceLocation.setLocationGroupName("STOCK");
        given(locationApi.findByErpCode("LOC2")).willReturn(Optional.of(sourceLocation));

        var m = new MovementVO();
        m.setTransportUnitBk("4713");
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

    @Sql(scripts = "classpath:import-TEST.sql")
    @Test void shall_complete_movement() throws Exception {
        var transportUnit = TransportUnitVO.newBuilder().barcode("4711").build();
        given(transportUnitApi.findTransportUnit("4711")).willReturn(transportUnit);
        var sourceLocation = new LocationVO("LOC_/0002/0000/0000/0000");
        sourceLocation.setErpCode("LOC2");
        sourceLocation.setLocationGroupName("STOCK");
        given(locationApi.findByErpCode("LOC2")).willReturn(Optional.of(sourceLocation));

        var m = new MovementVO();
        m.setTransportUnitBk("4711");
        m.setTarget("LOC2");
        m.setState("ACTIVE");
        mockMvc.perform(
                        patch("/v1/movements/1000/complete")
                                .content(objectMapper.writeValueAsString(m)).contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("move-complete"))
                .andExpect(jsonPath("$.state", is("DONE")))
                .andExpect(jsonPath("$.target", is("LOC2")))
                .andReturn().getResponse()
        ;
    }
}
