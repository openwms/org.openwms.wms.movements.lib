/*
 * Copyright 2005-2023 the original author or authors.
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
package org.openwms.wms.movements.spi.common;

import org.ameba.annotation.Measured;
import org.openwms.common.transport.api.commands.Command;
import org.openwms.core.SpringProfiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A NoOpTransportUnitApiImpl is a Spring managed bean that does nothing and is only active when Spring profile
 * {@linkplain SpringProfiles#ASYNCHRONOUS_PROFILE} is NOT activated.
 *
 * @author Heiko Scherrer
 */
@Profile("!" + SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
class NoOpTransportUnitApiImpl implements AsyncTransportUnitApi {

    /**
     * {@inheritDoc}
     */
    @Override
    @Measured
    public void process(Command command) {
        // Do nothing here
    }
}
