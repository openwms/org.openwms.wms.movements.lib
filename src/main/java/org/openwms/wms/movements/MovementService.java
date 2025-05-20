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
package org.openwms.wms.movements;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.openwms.wms.movements.api.MovementState;
import org.openwms.wms.movements.api.MovementType;
import org.openwms.wms.movements.api.MovementVO;
import org.openwms.wms.movements.api.ValidationGroups;

import java.util.List;

/**
 * A MovementService is the internal service API that deals with {@code Movement}s.
 *
 * @author Heiko Scherrer
 */
public interface MovementService {

    /**
     * Create a new {@code Movement} for a {@code TransportUnit}.
     *
     * @param bk The identifying business key of the TransportUnit to move
     * @param movement Detailed Movement information
     * @return The created Movement instance
     */
    @NotNull MovementVO create(
            @NotBlank(groups = ValidationGroups.Movement.Create.class) String bk,
            @NotNull(groups = ValidationGroups.Movement.Create.class) @Valid MovementVO movement);

    /**
     * Find and return {@code Movements} in the given {@code state} and of one of the {@code types}.
     *
     * @param state The state the Movement is in
     * @param source The source of the Movement
     * @param types The type of Movement
     * @return A list of, never {@literal null}
     */
    List<MovementVO> findFor(@NotNull MovementState state, @NotBlank String source, @NotEmpty MovementType... types);

    /**
     * Get all priorities as a list of strings.
     *
     * @return The list of priorities
     */
    List<String> getPriorityList();

    /**
     * Move a {@code Movement} to a new location.
     *
     * @param pKey The persistent key of the Movement
     * @param vo The Movement data must contain the new source
     * @return The moved instance
     */
    @NotNull MovementVO move(@NotBlank String pKey, @Valid @NotNull MovementVO vo);

    /**
     * Complete a {@code Movement}.
     *
     * @param pKey The identifying persistent key of the Movement to complete
     * @param vo Required data to set at completion
     * @return The completed instance
     */
    @NotNull MovementVO complete(@NotBlank String pKey, @Valid @NotNull MovementVO vo);

    /**
     * Cancel an existing {@code Movement}.
     *
     * @param pKey The identifying persistent key of the Movement to complete
     * @return The cancelled instance
     */
    @NotNull MovementVO cancel(@NotBlank String pKey);

    /**
     * Find and return all existing {@code Movement}s.
     *
     * @return All instances, never {@literal null}
     */
    @NotNull List<MovementVO> findAll();

    /**
     * Find all {@code Movement}s for a {@code TransportUnit} with the given {@code barcode} and {@code states}.
     *
     * @param barcode The business key of the TransportUnit to move
     * @param types A list of types to consider
     * @param states A list of states to consider
     * @return A list of Movements, never {@literal null}
     */
    @NotNull List<MovementVO> findForTuAndTypesAndStates(
            @NotBlank String barcode,
            @NotEmpty List<MovementType> types,
            @NotEmpty List<String> states);
}
