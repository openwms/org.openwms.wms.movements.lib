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
package org.openwms.wms.movements;

import org.openwms.transactions.api.commands.AsyncTransactionApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A MovementsRunner is the Spring Boot starter class of the microservice component.
 *
 * @author Heiko Scherrer
 */
@SpringBootApplication(scanBasePackageClasses = {
        MovementsRunner.class,
        AsyncTransactionApi.class
})
public class MovementsRunner {

    /**
     * Boot up!
     *
     * @param args Some args
     */
    public static void main(String[] args) {
        SpringApplication.run(MovementsRunner.class, args);
    }
}
