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
package org.openwms.transactions.api.commands;

import org.openwms.core.SpringProfiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A NoOpAsyncTransactionApi is a bean used as a placeholder in case the {@literal ASYNCHRONOUS} profile is not active.
 *
 * @author Heiko Scherrer
 */
@Profile("!" + SpringProfiles.ASYNCHRONOUS_PROFILE)
@Component
class NoOpAsyncTransactionApi implements AsyncTransactionApi {

    /**
     * {@inheritDoc}
     *
     * No operation
     */
    @Override
    public void process(TransactionCommand command) {
        // no operation here in this implementation
    }
}
