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
package org.openwms.wms.movements.commands;

import java.io.Serializable;

/**
 * A MovementMO.
 *
 * @author Heiko Scherrer
 */
public class MovementMO implements Serializable {

    private String pKey;
    private String transportUnitBK;
    private String initiator;
    private String target;

    public MovementMO() {
    }

    private MovementMO(Builder builder) {
        pKey = builder.pKey;
        transportUnitBK = builder.transportUnitBK;
        initiator = builder.initiator;
        target = builder.target;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getpKey() {
        return pKey;
    }

    public String getTransportUnitBK() {
        return transportUnitBK;
    }

    public String getInitiator() {
        return initiator;
    }

    public String getTarget() {
        return target;
    }

    public static final class Builder {
        private String pKey;
        private String transportUnitBK;
        private String initiator;
        private String target;

        private Builder() {
        }

        public Builder pKey(String val) {
            pKey = val;
            return this;
        }

        public Builder transportUnitBK(String val) {
            transportUnitBK = val;
            return this;
        }

        public Builder initiator(String val) {
            initiator = val;
            return this;
        }

        public Builder target(String val) {
            target = val;
            return this;
        }

        public MovementMO build() {
            return new MovementMO(this);
        }
    }
}
