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
package org.openwms.wms.movements.api;

/**
 * A ValidationGroups is a marker interface collection to aggregate all types used for bean validation groups.
 *
 * @author Heiko Scherrer
 */
public interface ValidationGroups {

    interface Movement {
        interface Create {}
        interface Move {}
        interface Complete {}
    }
}
