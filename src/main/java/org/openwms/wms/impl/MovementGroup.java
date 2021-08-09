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
package org.openwms.wms.impl;

import org.ameba.integration.jpa.ApplicationEntity;
import org.openwms.wms.api.MovementState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static org.openwms.wms.spi.DefaultMovementState.ACTIVE;

/**
 * A MovementGroup is used to group {@link Movement}s.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_MOVEMENT_GROUP")
public class MovementGroup extends ApplicationEntity implements Serializable {

    @Column(name = "C_NAME", nullable = false)
    @NotEmpty
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "C_STATE")
    private MovementState state;

    /** A human user might be assigned to this {@code MovementGroup}. */
    @Column(name = "C_USER")
    private String assignedUser;

    /** The {@link Movement}s that belong to the {@code MovementGroup}. */
    @OneToMany(mappedBy = "group", cascade = {PERSIST, MERGE})
    private Set<Movement> movements = new HashSet<>();

    /*~ -------------- Constructors -------------- */
    /** Dear JPA... */
    protected MovementGroup() {}

    public MovementGroup(String name) {
        this.name = name;
    }

    /*~ --------------- Lifecycle ---------------- */
    @PrePersist
    protected void prePersist() {
        this.state = ACTIVE;
    }

    /*~ --------------- Accessors ---------------- */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Set<Movement> getMovements() {
        return movements;
    }

    public void setMovements(Set<Movement> movements) {
        this.movements = movements;
    }

    /**
     * {@inheritDoc}
     *
     * All fields
     */
    @Override
    public String toString() {
        return "MovementGroup{" +
                "name='" + name + '\'' +
                ", state=" + state +
                ", assignedUser='" + assignedUser + '\'' +
                ", movements=" + movements +
                '}';
    }

    /**
     * {@inheritDoc}
     *
     * All fields
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovementGroup)) return false;
        if (!super.equals(o)) return false;
        MovementGroup that = (MovementGroup) o;
        return Objects.equals(name, that.name) && Objects.equals(state, that.state) && Objects.equals(assignedUser, that.assignedUser) && Objects.equals(movements, that.movements);
    }

    /**
     * {@inheritDoc}
     *
     * All fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, state, assignedUser, movements);
    }
}
