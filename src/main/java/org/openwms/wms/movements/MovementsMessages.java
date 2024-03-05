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
package org.openwms.wms.movements;

/**
 * A MovementsMessages.
 *
 * @author Heiko Scherrer
 */
public final class MovementsMessages {

    /** If a Location has not been found by an erpCode. */
    public static final String LOCATION_NOT_FOUND_BY_ERP_CODE= "owms.wms.mov.loc.erpCodeNotExists";
    /** If a Location has not been found by the LocationId. */
    public static final String LOCATION_NOT_FOUND_BY_ID = "owms.wms.mov.loc.idNotExists";
    /** If the Movement with the given pKey does not exist. */
    public static final String MOVEMENT_NOT_FOUND = "owms.wms.mov.mov.notFound";
    /** The Movement can't be moved because it is already completed. */
    public static final String MOVEMENT_COMPLETED_NOT_MOVED = "owms.wms.mov.mov.completedNotMoved";

    /** Message about a move. */
    public static final String MSG_MOVEMENT_MOVED = "owms.wms.mov.msg.mov.moved";
    /** Message about a completed movement. */
    public static final String MSG_MOVEMENT_COMPLETED = "owms.wms.mov.msg.mov.completed";
    private MovementsMessages() {}
}
