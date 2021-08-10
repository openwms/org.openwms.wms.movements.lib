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
package org.openwms.wms.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.ameba.http.AbstractBase;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

import static org.openwms.wms.impl.ValidationGroups.Movement.Complete;
import static org.openwms.wms.impl.ValidationGroups.Movement.Create;
import static org.openwms.wms.impl.ValidationGroups.Movement.Move;
/**
 * A MovementVO encapsulates details about the actual request to move a {@code TransportUnit} to a target.
 *
 * @author Heiko Scherrer
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class MovementVO extends AbstractBase implements Serializable {

    /** The persistent key is returned from the service as soon as the {@code Movement} has been created. */
    @JsonProperty("pKey")
    private String persistentKey;
    /** The business key of the {@code TransportUnit} to move. */
    @JsonProperty("transportUnitBk")
    private String transportUnitBk;
    /** Whether the {@code Movement} should be directly processed (AUTOMATIC) or delayed (MANUAL). */
    @JsonProperty("mode")
    private StartMode startMode = StartMode.AUTOMATIC;
    /** A priority how fast and prio the {@code Movement} needs to be processed; A higher value means less prio than lower values. */
    @JsonProperty("priority")
    private Integer priority;
    /** The state of the {@code Movement}. */
    @JsonProperty("state")
    @NotEmpty(groups = Move.class)
    private String state;
    /** The type of {@code Movement} must be passed by the caller. */
    @JsonProperty("type") // Not required at creation because it can be resolved from the TU and the target
    private MovementType type;
    /** The source {@code Location} where the {@code TransportUnit} shall be picked up (must be passed by the caller). */
    @JsonProperty("sourceLocation")
    @NotEmpty(groups = {Create.class, Move.class})
    private String sourceLocation;
    /** The {@code LocationGroup} the {@code sourceLocation} belongs to. */
    @JsonProperty("sourceLocationGroupName")
    private String sourceLocationGroupName;
    /** The target where to move the {@code TransportUnit} to (must be passed by the caller). */
    @JsonProperty("target")
    @NotEmpty(groups = {Create.class, Complete.class})
    private String target;

    /**
     * Checks whether a target is set or not.
     *
     * @return {@literal true} if so
     */
    public boolean hasTarget() {
        return target != null && !target.isEmpty();
    }
}
