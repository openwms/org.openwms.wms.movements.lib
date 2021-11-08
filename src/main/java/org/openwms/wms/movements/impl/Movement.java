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
package org.openwms.wms.movements.impl;

import org.ameba.integration.jpa.ApplicationEntity;
import org.openwms.common.transport.Barcode;
import org.openwms.wms.movements.Message;
import org.openwms.wms.movements.api.MovementType;
import org.openwms.wms.movements.api.StartMode;
import org.openwms.wms.movements.spi.DefaultMovementState;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.openwms.wms.movements.MovementConstants.DATE_TIME_WITH_TIMEZONE;

/**
 * A Movement is a simple task to move a {@code TransportUnit} from one source {@code Location} to a target {@code Location}. This is
 * often used for manual warehouses or manual activities.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_MOVEMENT")
public class Movement extends ApplicationEntity implements Serializable {

    /** The business key of the {@code TransportUnit} to move. */
    @NotNull
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "C_TRANSPORT_UNIT_BK", nullable = false))
    private Barcode transportUnitBk;

    /** Type of {@code Movement}. */
    @Column(name = "C_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType type;

    /** The {@link MovementGroup}, the {@code Movement} belongs to. */
    @ManyToOne
    @JoinColumn(name = "C_GROUP_PK", nullable = true, foreignKey = @ForeignKey(name = "FK_MVM_GRP"))
    private MovementGroup group;

    @Column(name = "C_SKU")
    private String sku;

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

    /** The current state the {@link Movement} resides in. */
    @Column(name = "C_STATE")
    @Enumerated(EnumType.STRING)
    private DefaultMovementState state;

    /** A message with the reason for this {@code Movement}. */
    @Embedded
    private Message message;

    /** Reported problems on the {@code Movement}. */
    @OneToMany(mappedBy = "movement", cascade = CascadeType.ALL)
    private List<ProblemHistory> problems;

    /** Where the {@code Movement} is picked up. */
    @Column(name = "C_SOURCE_LOCATION")
    private String sourceLocation;

    /** The name of the {@code LocationGroup} where the {@code sourceLocation} belongs to. */
    @Column(name = "C_SOURCE_LOCATION_GROUP_NAME")
    private String sourceLocationGroupName;

    /** The target {@code Location} of the {@code Movement}. This property is set before the {@code Movement} is started. */
    @Column(name = "C_TARGET_LOCATION")
    @Null(groups = ValidationGroups.Movement.Create.class)
    private String targetLocation;

    /** A {@code LocationGroup} can also be set as target. At least one target must be set when the {@code Movement} is being started. */
    @Column(name = "C_TARGET_LOCATION_GROUP_NAME")
    @NotNull
    private String targetLocationGroup;

    /** Date when the {@code Movement} can be started earliest. */
    @Column(name = "C_START_EARLIEST_DATE", columnDefinition = "timestamp(0)")
    @DateTimeFormat(pattern = DATE_TIME_WITH_TIMEZONE)
    private ZonedDateTime startEarliestDate;

    /** Date when the {@code Movement} was started. */
    @Column(name = "C_START_DATE", columnDefinition = "timestamp(0)")
    @DateTimeFormat(pattern = DATE_TIME_WITH_TIMEZONE)
    private ZonedDateTime startDate;

    /** Latest possible finish date of this {@code Movement}. */
    @Column(name = "C_LATEST_DUE", columnDefinition = "timestamp(0)")
    @DateTimeFormat(pattern = DATE_TIME_WITH_TIMEZONE)
    private ZonedDateTime latestDueDate;

    /** Date when the {@code Movement} ended. */
    @Column(name = "C_END_DATE", columnDefinition = "timestamp(0)")
    @DateTimeFormat(pattern = DATE_TIME_WITH_TIMEZONE)
    private ZonedDateTime endDate;

    /*~ -------------- Constructors -------------- */
    /** Dear JPA... */
    protected Movement() {}

    /*~ ---------------- Methods ----------------- */

    /**
     * Add a new problem to the {@code Movement}s {@code problemHistory}.
     *
     * @param problem The problem to store
     * @return {@literal true} if added successfully
     */
    public boolean addProblem(ProblemHistory problem) {
        if (this.problems == null) {
            this.problems = new ArrayList<>(1);
        }
        return this.problems.add(problem);
    }

    /**
     * Check whether the {@code targetLocation} is empty.
     *
     * @return {@literal true} if so
     */
    public boolean emptyTargetLocation() {
        return targetLocation == null || targetLocation.isEmpty();
    }

    /*~ --------------- Accessors ---------------- */
    public Barcode getTransportUnitBk() {
        return transportUnitBk;
    }

    public void setTransportUnitBk(Barcode transportUnitBk) {
        this.transportUnitBk = transportUnitBk;
    }

    public MovementType getType() {
        return type;
    }

    public MovementGroup getGroup() {
        return group;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public void setMessage(Message message) {
        this.message = message;
    }

    public DefaultMovementState getState() {
        return state;
    }

    public void setState(DefaultMovementState state) {
        this.state = state;
    }

    public List<ProblemHistory> getProblems() {
        return problems;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getSourceLocationGroupName() {
        return sourceLocationGroupName;
    }

    public void setSourceLocationGroupName(String sourceLocationGroupName) {
        this.sourceLocationGroupName = sourceLocationGroupName;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(String targetLocation) {
        this.targetLocation = targetLocation;
    }

    public String getTargetLocationGroup() {
        return targetLocationGroup;
    }

    public boolean emptyTargetLocationGroup() {
        return targetLocationGroup == null;
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

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getLatestDueDate() {
        return latestDueDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * {@inheritDoc}
     *
     * Not the group and not the history.
     */
    @Override
    public String toString() {
        return "Movement{" +
                "transportUnitBk=" + transportUnitBk +
                ", type=" + type +
                ", priority=" + priority +
                ", mode=" + mode +
                ", state=" + state +
                ", message=" + message +
                ", sourceLocation='" + sourceLocation + '\'' +
                ", sourceLocationGroupName='" + sourceLocationGroupName + '\'' +
                ", targetLocation='" + targetLocation + '\'' +
                ", targetLocationGroup='" + targetLocationGroup + '\'' +
                ", startEarliestDate=" + startEarliestDate +
                ", startDate=" + startDate +
                ", latestDueDate=" + latestDueDate +
                ", endDate=" + endDate +
                '}';
    }

    /**
     * {@inheritDoc}
     *
     * Not the group and not the history.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movement)) return false;
        if (!super.equals(o)) return false;
        Movement movement = (Movement) o;
        return Objects.equals(transportUnitBk, movement.transportUnitBk) && type == movement.type && priority == movement.priority && mode == movement.mode && state == movement.state && Objects.equals(message, movement.message) && Objects.equals(sourceLocation, movement.sourceLocation) && Objects.equals(sourceLocationGroupName, movement.sourceLocationGroupName) && Objects.equals(targetLocation, movement.targetLocation) && Objects.equals(targetLocationGroup, movement.targetLocationGroup) && Objects.equals(startEarliestDate, movement.startEarliestDate) && Objects.equals(startDate, movement.startDate) && Objects.equals(latestDueDate, movement.latestDueDate) && Objects.equals(endDate, movement.endDate);
    }

    /**
     * {@inheritDoc}
     *
     * Not the group and not the history.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transportUnitBk, type, priority, mode, state, message, sourceLocation, sourceLocationGroupName, targetLocation, targetLocationGroup, startEarliestDate, startDate, latestDueDate, endDate);
    }
}
