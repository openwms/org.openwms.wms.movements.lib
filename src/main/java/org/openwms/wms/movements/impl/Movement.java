/*
 * Copyright 2005-2026 the original author or authors.
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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.ameba.integration.jpa.ApplicationEntity;
import org.openwms.common.transport.Barcode;
import org.openwms.wms.movements.Message;
import org.openwms.wms.movements.api.MovementType;
import org.openwms.wms.movements.api.StartMode;
import org.openwms.wms.movements.api.ValidationGroups;
import org.openwms.wms.movements.spi.DefaultMovementState;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.Assert;

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

    /** Type of the {@code Movement}. */
    @Column(name = "C_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType type;

    /** Initiator of the {@code Movement}, who ordered or triggered it. */
    @Column(name = "C_INITIATOR", nullable = false)
    @NotNull
    private String initiator;

    /** The {@link MovementGroup}, the {@code Movement} belongs to. */
    @ManyToOne
    @JoinColumn(name = "C_GROUP_PK", nullable = true, foreignKey = @ForeignKey(name = "FK_MVM_GRP"))
    private MovementGroup group;

    /** Refers to the demanded {@code Product} for that the {@code Movement} has been created. */
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

    @Override
    public void setPersistentKey(String pKey) {
        super.setPersistentKey(pKey);
    }

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

    /**
     * Set the initiator, or the given default value.
     *
     * @param initiator The initiator
     * @param defaultVal The default value
     */
    public void setInitiatorOrDefault(String initiator, String defaultVal) {
        if (initiator == null || initiator.isEmpty()) {
            Assert.hasText(defaultVal, "The default value for initiator must be given");
            this.initiator = defaultVal;
        } else {
            this.initiator = initiator;
        }
    }

    /**
     * Checks if this {@code Movement} has a {@code SKU} set.
     *
     * @return {@literal true} if so
     */
    public boolean hasSKU() {
        return this.sku != null && !this.sku.isEmpty();
    }

    /**
     * Set a {@code startDate} for this {@code Movement} if not already set.
     *
     * @param startDate The start date to set
     */
    public void initStartDate(ZonedDateTime startDate) {
        if (this.startDate == null) {
            this.startDate = startDate;
        }
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

    public void setType(MovementType type) {
        this.type = type;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
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

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
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
                ", pKey=" + getPersistentKey() +
                ", type=" + type +
                ", initiator=" + initiator +
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
        return Objects.equals(transportUnitBk, movement.transportUnitBk) && type == movement.type && Objects.equals(initiator, movement.initiator) && Objects.equals(sku, movement.sku) && priority == movement.priority && mode == movement.mode && state == movement.state && Objects.equals(message, movement.message) && Objects.equals(sourceLocation, movement.sourceLocation) && Objects.equals(sourceLocationGroupName, movement.sourceLocationGroupName) && Objects.equals(targetLocation, movement.targetLocation) && Objects.equals(targetLocationGroup, movement.targetLocationGroup) && Objects.equals(startEarliestDate, movement.startEarliestDate) && Objects.equals(startDate, movement.startDate) && Objects.equals(latestDueDate, movement.latestDueDate) && Objects.equals(endDate, movement.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), transportUnitBk, type, initiator, sku, priority, mode, state, message, sourceLocation, sourceLocationGroupName, targetLocation, targetLocationGroup, startEarliestDate, startDate, latestDueDate, endDate);
    }
}
