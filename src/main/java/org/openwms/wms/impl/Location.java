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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Location.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_LOCATION")
public class Location extends ApplicationEntity implements Serializable {

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "area", column = @Column(name = "C_AREA")),
            @AttributeOverride(name = "aisle", column = @Column(name = "C_AISLE")),
            @AttributeOverride(name = "x", column = @Column(name = "C_X")),
            @AttributeOverride(name = "y", column = @Column(name = "C_Y")),
            @AttributeOverride(name = "z", column = @Column(name = "C_Z"))
    })
    private LocationPK locationId;

    @Column(name = "C_LOCATION_GROUP_NAME")
    private String locationGroupName;

    protected Location() {
    }

    public Location(LocationPK locationId) {
        this.locationId = locationId;
    }

    public LocationPK getLocationId() {
        return locationId;
    }

    public String getLocationGroupName() {
        return locationGroupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Location location = (Location) o;
        return locationId.equals(location.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), locationId);
    }
}
