/*
 * Copyright 2005-2024 the original author or authors.
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

import org.ameba.annotation.Measured;
import org.ameba.exception.NotFoundException;
import org.ameba.http.ctx.CallContext;
import org.ameba.http.ctx.CallContextHolder;
import org.ameba.http.identity.IdentityContextHolder;
import org.ameba.i18n.Translator;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.messages.LocationMO;
import org.openwms.common.transport.api.commands.TUCommand;
import org.openwms.common.transport.api.messages.TransportUnitMO;
import org.openwms.core.SpringProfiles;
import org.openwms.transactions.api.TransactionBuilder;
import org.openwms.transactions.api.commands.AsyncTransactionApi;
import org.openwms.transactions.api.commands.TransactionCommand;
import org.openwms.wms.movements.spi.common.AsyncTransportUnitApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.openwms.transactions.api.commands.TransactionCommand.Type.CREATE;
import static org.openwms.wms.movements.MovementsMessages.LOCATION_NOT_FOUND_BY_ERP_CODE;
import static org.openwms.wms.movements.MovementsMessages.MSG_MOVEMENT_COMPLETED;
import static org.openwms.wms.movements.MovementsMessages.MSG_MOVEMENT_MOVED;

/**
 * A TransactionWriter is a Spring managed bean, activated with {@value SpringProfiles#ASYNCHRONOUS_PROFILE} profile that takes care of
 * writing business transactions via the COMMON Transaction service.
 *
 * @author Heiko Scherrer
 */
@Profile(SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
@RefreshScope
class TransactionWriter {

    private final String applicationName;
    private final Translator translator;
    private final LocationApi locationApi;
    private final AsyncTransportUnitApi asyncTransportUnitApi;
    private final AsyncTransactionApi asyncTransactionApi;

    TransactionWriter(@Value("${spring.application.name}") String applicationName, Translator translator, LocationApi locationApi, AsyncTransportUnitApi asyncTransportUnitApi,
            AsyncTransactionApi asyncTransactionApi) {
        this.applicationName = applicationName;
        this.translator = translator;
        this.locationApi = locationApi;
        this.asyncTransportUnitApi = asyncTransportUnitApi;
        this.asyncTransactionApi = asyncTransactionApi;
    }

    @Measured
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEvent(MovementEvent event) {
        switch (event.getType()) {
            case MOVED:
                asyncTransactionApi.process(TransactionCommand.of(CREATE,
                        TransactionBuilder.aTransactionVO()
                                .withCreatedByUser(IdentityContextHolder.getCurrentIdentity())
                                .withCategory(CallContextHolder.getOptionalCallContext().map(CallContext::getCaller).orElse(""))
                                .withSender(applicationName)
                                .withType(MSG_MOVEMENT_MOVED)
                                .withDescription(translator.translate(MSG_MOVEMENT_MOVED,
                                        event.getSource().getTransportUnitBk(),
                                        event.getPreviousLocation(),
                                        event.getSource().getSourceLocation()))
                                .withDetail("transportUnitBK", event.getSource().getTransportUnitBk().getValue())
                                .withDetail("previousLocation", event.getPreviousLocation())
                                .withDetail("actualLocation", event.getSource().getSourceLocation())
                                .build()
                ));
                break;
            case COMPLETED:
                asyncTransactionApi.process(TransactionCommand.of(CREATE,
                        TransactionBuilder.aTransactionVO()
                                .withCreatedByUser(IdentityContextHolder.getCurrentIdentity())
                                .withCategory(CallContextHolder.getOptionalCallContext().map(CallContext::getCaller).orElse(""))
                                .withSender(applicationName)
                                .withType(MSG_MOVEMENT_COMPLETED)
                                .withDescription(translator.translate(MSG_MOVEMENT_COMPLETED,
                                        event.getSource().getTransportUnitBk(),
                                        event.getSource().getSourceLocation(),
                                        event.getPreviousLocation())
                                )
                                .withDetail("transportUnitBK", event.getSource().getTransportUnitBk().getValue())
                                .withDetail("previousLocation", event.getSource().getSourceLocation())
                                .withDetail("actualLocation", event.getPreviousLocation())
                                .build()
                ));
                break;
            default:
        }
    }

    @Measured
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEvent(MovementTargetChangedEvent event) {
        var locationByErpCode = locationApi.findByErpCode(event.getSource().getTargetLocation());
        if (locationByErpCode.isPresent()) {
            asyncTransportUnitApi.process(
                    TUCommand.newBuilder(TUCommand.Type.CHANGE_TARGET)
                            .withTransportUnit(TransportUnitMO.newBuilder()
                                    .withBarcode(event.getSource().getTransportUnitBk().getValue())
                                    .withTargetLocation(LocationMO.ofId(locationByErpCode.get().getLocationId()))
                                    .build()
                            )
                            .build()
            );
        } else {
            throw new NotFoundException(translator, LOCATION_NOT_FOUND_BY_ERP_CODE, new String[]{event.getSource().getTargetLocation()},
                    event.getSource().getTargetLocation());
        }
    }
}
