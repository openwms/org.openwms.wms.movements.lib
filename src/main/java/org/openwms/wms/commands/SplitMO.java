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
package org.openwms.wms.commands;

import java.io.Serializable;

/**
 * A SplitMO.
 *
 * @author Heiko Scherrer
 */
public class SplitMO implements Serializable {

    private String reservationId;
    private String shippingOrderPositionPKey;
    private int splitNo;
    private int priority;
    private String transportUnitBK;
    private String targetName;

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getShippingOrderPositionPKey() {
        return shippingOrderPositionPKey;
    }

    public void setShippingOrderPositionPKey(String shippingOrderPositionPKey) {
        this.shippingOrderPositionPKey = shippingOrderPositionPKey;
    }

    public int getSplitNo() {
        return splitNo;
    }

    public void setSplitNo(int splitNo) {
        this.splitNo = splitNo;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTransportUnitBK() {
        return transportUnitBK;
    }

    public void setTransportUnitBK(String transportUnitBK) {
        this.transportUnitBK = transportUnitBK;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}
