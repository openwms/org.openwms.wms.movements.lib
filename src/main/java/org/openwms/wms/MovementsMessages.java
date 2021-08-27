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
package org.openwms.wms;

/**
 * A MovementsMessages.
 *
 * @author Heiko Scherrer
 */
public final class MovementsMessages {

    public static final String LOCATION_NOT_FOUND_BY_ERP_CODE= "owms.wms.mov.loc.erpCodeNotExists";
    public static final String LOCATION_NOT_FOUND_BY_ID = "owms.wms.mov.loc.idNotExists";
    public static final String MOVEMENT_NOT_FOUND = "owms.wms.mov.mov.notMoved";
    public static final String MOVEMENT_COMPLETED_NOT_MOVED = "owms.wms.mov.mov.completedNotMoved";

    private MovementsMessages() {}
}
