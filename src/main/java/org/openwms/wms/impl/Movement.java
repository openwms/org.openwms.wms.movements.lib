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
import org.openwms.common.transport.Barcode;
import org.openwms.wms.Message;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.StartMode;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A Movement.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_MOVEMENT")
public class Movement extends ApplicationEntity implements Serializable {

    /** The business key of the {@code TransportUnit} to move. */
    @NotNull(groups = ValidationGroups.Movement.Create.class)
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "C_TRANSPORT_UNIT_BK", length = Barcode.BARCODE_LENGTH, nullable = false))
    private Barcode transportUnitBk;

    /** Type of movement. */
    @Column(name = "C_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType type;

    /**
     * A priority level of the {@code Movement}. The lower the value the lower the priority. The priority level affects the execution of the
     * {@code Movement}. An order with high priority will be processed faster than those with lower priority.
     */
    @Column(name = "C_PRIORITY", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private PriorityLevel priority;

    /** Defines how the resulting {@code TransportOrder} is started. */
    @Column(name = "C_MODE", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private StartMode mode = StartMode.MANUAL;

    /** A message with the reason for this {@code Movement}. */
    @Embedded
    private Message message;

    /** Reported problems on the {@code Movement}. */
    @OneToOne
    private ProblemHistory problem;

    /** The target {@code Location} of the {@code Movement}. This property is set before the {@code Movement} is started. */
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "C_TARGET_LOCATION")
    private Location targetLocation;

    /** A {@code LocationGroup} can also be set as target. At least one target must be set when the {@code Movement} is being started. */
    @Column(name = "C_TARGET_LOCATION_GROUP")
    private String targetLocationGroup;

    /** Date when the {@code Movement} can be started earliest. */
    @Column(name = "C_START_EARLIEST_DATE")
    private ZonedDateTime startEarliestDate;

    /** Date when the {@code Movement} was started. */
    @Column(name = "C_START_DATE")
    private ZonedDateTime startDate;

    /** Latest possible finish date of this {@code Movement}. */
    @Column(name = "C_LATEST_DUE")
    private ZonedDateTime latestDueDate;

    /** Date when the {@code Movement} ended. */
    @Column(name = "C_END_DATE")
    private ZonedDateTime endDate;

    public Movement() {
    }

    public Barcode getTransportUnitBk() {
        return transportUnitBk;
    }

    public void setTransportUnitBk(Barcode transportUnitBk) {
        this.transportUnitBk = transportUnitBk;
    }

    public MovementType getType() {
        return type;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public StartMode getMode() {
        return mode;
    }

    public void setMode(StartMode mode) {
        this.mode = mode;
    }

    public Message getMessage() {
        return message;
    }

    public ProblemHistory getProblem() {
        return problem;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    public String getTargetLocationGroup() {
        return targetLocationGroup;
    }

    public void setTargetLocationGroup(String targetLocationGroup) {
        this.targetLocationGroup = targetLocationGroup;
    }

    public ZonedDateTime getStartEarliestDate() {
        return startEarliestDate;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public ZonedDateTime getLatestDueDate() {
        return latestDueDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return "Movement{" +
                "transportUnitBk=" + transportUnitBk +
                ", type=" + type +
                ", priority=" + priority +
                ", mode=" + mode +
                ", message=" + message +
                ", problem=" + problem +
                ", targetLocation=" + targetLocation +
                ", targetLocationGroup='" + targetLocationGroup + '\'' +
                ", startEarliestDate=" + startEarliestDate +
                ", startDate=" + startDate +
                ", latestDueDate=" + latestDueDate +
                ", endDate=" + endDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movement movement = (Movement) o;
        return Objects.equals(transportUnitBk, movement.transportUnitBk) &&
                type == movement.type &&
                priority == movement.priority &&
                mode == movement.mode &&
                Objects.equals(message, movement.message) &&
                Objects.equals(problem, movement.problem) &&
                Objects.equals(targetLocation, movement.targetLocation) &&
                Objects.equals(targetLocationGroup, movement.targetLocationGroup) &&
                Objects.equals(startEarliestDate, movement.startEarliestDate) &&
                Objects.equals(startDate, movement.startDate) &&
                Objects.equals(latestDueDate, movement.latestDueDate) &&
                Objects.equals(endDate, movement.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transportUnitBk, type, priority, mode, message, problem, targetLocation, targetLocationGroup, startEarliestDate, startDate, latestDueDate, endDate);
    }
}
