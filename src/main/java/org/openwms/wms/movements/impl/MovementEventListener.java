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

import org.ameba.exception.NotFoundException;
import org.ameba.i18n.Translator;
import org.openwms.common.location.api.LocationApi;
import org.openwms.common.location.api.LocationVO;
import org.openwms.common.location.api.messages.LocationMO;
import org.openwms.common.transport.api.commands.TUCommand;
import org.openwms.common.transport.api.messages.TransportUnitMO;
import org.openwms.wms.movements.spi.common.AsyncTransportUnitApi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

import static org.openwms.wms.movements.MovementsMessages.LOCATION_NOT_FOUND_BY_ERP_CODE;

/**
 * A MovementEventListener.
 *
 * @author Heiko Scherrer
 */
@Component
class MovementEventListener {

    private final Translator translator;
    private final LocationApi locationApi;
    private final AsyncTransportUnitApi asyncTransportUnitApi;

    MovementEventListener(Translator translator, LocationApi locationApi, AsyncTransportUnitApi asyncTransportUnitApi) {
        this.translator = translator;
        this.locationApi = locationApi;
        this.asyncTransportUnitApi = asyncTransportUnitApi;
    }

    @TransactionalEventListener
    public void onEvent(MovementTargetChangedEvent event) {
        Optional<LocationVO> locationByErpCode = locationApi.findByErpCode(event.getSource().getTargetLocation());
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
