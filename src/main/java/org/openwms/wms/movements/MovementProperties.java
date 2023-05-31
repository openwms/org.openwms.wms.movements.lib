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
package org.openwms.wms.movements;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * A MovementProperties.
 *
 * @author Heiko Scherrer
 */
@Configuration
@ConfigurationProperties("owms.movement")
public class MovementProperties {

    /** Shall the Putaway API be called to resolve a final target for Movements. */
    private Boolean putawayResolutionEnabled;

    /** Target configurations. */
    private List<MovementTarget> targets = new ArrayList<>();

    public Boolean getPutawayResolutionEnabled() {
        return putawayResolutionEnabled;
    }

    public void setPutawayResolutionEnabled(Boolean putawayResolutionEnabled) {
        this.putawayResolutionEnabled = putawayResolutionEnabled;
    }

    public MovementTarget findTarget(String name) {
        return targets.stream().filter(t -> t.getName().equals(name))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(format("No target [%s] configured in application configuration", name)));
    }

    public List<MovementTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<MovementTarget> targets) {
        this.targets = targets;
    }
}
