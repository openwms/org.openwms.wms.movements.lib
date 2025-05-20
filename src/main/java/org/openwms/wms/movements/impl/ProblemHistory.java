/*
 * Copyright 2005-2025 the original author or authors.
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

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.ameba.integration.jpa.ApplicationEntity;
import org.openwms.wms.movements.Message;

import java.io.Serializable;
import java.util.Objects;

/**
 * A ProblemHistory stores an occurred problem, in form of {@code Message}, recorded on a {@code Movement}.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_PROBLEM_HISTORY")
public class ProblemHistory extends ApplicationEntity implements Serializable {

    /** Reference to the {@link Movement} it belongs to. */
    @ManyToOne
    @JoinColumn(name = "C_FK_MOVEMENT", foreignKey = @ForeignKey(name = "FK_PHISTORY_MVM"))
    private Movement movement;

    /** The message to store in the history. */
    @Embedded
    private Message problem;

    /** Dear JPA ... */
    protected ProblemHistory() {}

    /**
     * Full constructor.
     *
     * @param movement The Movement this problem initially occurred
     * @param problem The problem itself
     */
    public ProblemHistory(Movement movement, Message problem) {
        this.movement = movement;
        this.problem = problem;
    }

    /**
     * Get the problem.
     *
     * @return The problem
     */
    public Message getProblem() {
        return problem;
    }

    /**
     * Get the corresponding {@code Movement}.
     *
     * @return The movement
     */
    public Movement getMovement() {
        return movement;
    }

    /**
     * {@inheritDoc}
     *
     * All fields
     */
    @Override
    public String toString() {
        return "ProblemHistory{" +
                "movement=" + movement +
                ", problem=" + problem +
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
        if (!(o instanceof ProblemHistory)) return false;
        if (!super.equals(o)) return false;
        ProblemHistory that = (ProblemHistory) o;
        return Objects.equals(movement, that.movement) && Objects.equals(problem, that.problem);
    }

    /**
     * {@inheritDoc}
     *
     * All fields
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), movement, problem);
    }
}
