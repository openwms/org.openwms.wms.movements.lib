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
package org.openwms.tms.impl;

import org.ameba.integration.jpa.ApplicationEntity;
import org.openwms.common.transport.Barcode;
import org.openwms.tms.Message;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * A Movement.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_MOVEMENT")
class Movement extends ApplicationEntity implements Serializable {

    private Barcode transportUnitBk;

    private MovementType type;

    /**
     * A priority level of the {@code Movement}. The lower the value the lower the priority. The priority level affects the execution of the
     * {@code Movement}. An order with high priority will be processed faster than those with lower priority.
     */
    @Column(name = "C_PRIORITY")
    @Enumerated(EnumType.STRING)
    private PriorityLevel priority = PriorityLevel.NORMAL;

    /** A message with the reason for this {@code Movement}. */
    @Embedded
    private Message message;

    /** Reported problems on the {@code Movement}. */
    @Embedded
    private ProblemHistory problem;

    /** The target {@code Location} of the {@code Movement}. This property is set before the {@code Movement} is started. */
    @Column(name = "C_TARGET_LOCATION")
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
}
