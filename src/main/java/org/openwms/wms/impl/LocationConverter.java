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

import org.dozer.DozerConverter;
import org.openwms.common.location.LocationPK;

/**
 * A LocationConverter.
 *
 * @author Heiko Scherrer
 */
public class LocationConverter extends DozerConverter<String, Movement> {

    public LocationConverter() {
        super(String.class, Movement.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Movement convertTo(String source, Movement destination) {
        if (source == null) {
            return null;
        }
        if (LocationPK.isValid(source)) {
            destination.setTargetLocation(new Location(LocationPK.fromString(source)));
        } else {
            destination.setTargetLocationGroup(source);
        }
        return destination;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convertFrom(Movement source, String destination) {
        if (source == null) {
            return null;
        }
        if (source.getTargetLocation() != null) {
            return source.getTargetLocation().getLocationId().toString();
        }
        return source.getTargetLocationGroup();
    }
}
