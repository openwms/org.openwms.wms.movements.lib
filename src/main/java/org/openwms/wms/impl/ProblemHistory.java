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
import org.openwms.wms.Message;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * A ProblemHistory stores an occurred problem, in form of {@code Message}, recorded on a {@code Movement}.
 *
 * @author Heiko Scherrer
 */
@Entity
@Table(name = "MVM_PROBLEM_HISTORY")
public class ProblemHistory extends ApplicationEntity implements Serializable {

    @ManyToOne
    @JoinColumn(name = "C_FK_MOVEMENT", foreignKey = @ForeignKey(name = "FK_PHISTORY_MVM"))
    private Movement movement;

    @Embedded
    private Message problem;

    /** Dear JPA ... */
    protected ProblemHistory() {
    }

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
}
