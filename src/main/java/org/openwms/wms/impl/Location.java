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
package org.openwms.wms.impl;

import org.ameba.integration.jpa.ApplicationEntity;
import org.openwms.common.location.LocationPK;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A Location.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_LOCATION")
public class Location extends ApplicationEntity implements Serializable {

    @NotNull
    @Column(name = "C_LOCATION_ID")
    private LocationPK locationId;
    @Column(name = "C_LOCATION_GROUP_NAME")
    private String locationGroupName;

    protected Location() {
    }

    public LocationPK getLocationId() {
        return locationId;
    }

    public String getLocationGroupName() {
        return locationGroupName;
    }
}
