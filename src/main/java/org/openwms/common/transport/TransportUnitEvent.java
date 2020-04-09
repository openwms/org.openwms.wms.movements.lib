/*
 * Copyright 2005-2020 the original author or authors.
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
package org.openwms.common.transport;

import org.openwms.common.transport.api.TransportUnitVO;
import org.openwms.core.event.RootApplicationEvent;

/**
 * A TransportUnitEvent.
 *
 * @author Heiko Scherrer
 */
public class TransportUnitEvent extends RootApplicationEvent {

    private TransportUnitEventType type;

    private TransportUnitEvent(TransportUnitVO source, TransportUnitEventType type) {
        super(source);
        this.type = type;
    }

    private TransportUnitEvent(Builder builder) {
        super(builder.source);
        type = builder.type;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public TransportUnitEventType getType() {
        return type;
    }

    public static TransportUnitEvent of(TransportUnitVO tu, TransportUnitEventType type) {
        return new TransportUnitEvent(tu, type);
    }

    public enum TransportUnitEventType {
        CHANGE_TARGET;
    }

    public static final class Builder {
        private TransportUnitVO source;
        private TransportUnitEventType type;

        private Builder() {
        }

        public Builder tu(TransportUnitVO val) {
            source = val;
            return this;
        }

        public Builder type(TransportUnitEventType val) {
            type = val;
            return this;
        }

        public TransportUnitEvent build() {
            return new TransportUnitEvent(this);
        }
    }
}
