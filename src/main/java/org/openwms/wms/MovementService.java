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
package org.openwms.wms;

import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.MovementVO;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A MovementService.
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
    MovementVO create(@NotEmpty String bk, @NotNull MovementVO movement);

    /**
     * Find and return {@code Movements} in the given {@code state} and of one of the {@code types}.
     *
     * @param state The state the Movement is in
     * @param source The source of the Movement
     * @param types The type of Movement
     * @return A list of, never {@literal null}
     */
    List<MovementVO> findFor(@NotEmpty String state, @NotEmpty String source, @NotNull MovementType... types);

    /**
     *
     * @return
     */
    List<String> getPriorityList();

    /**
     *
     * @param pKey
     * @param vo
     * @return
     */
    MovementVO move(@NotEmpty String pKey, @NotNull MovementVO vo);

    /**
     * Complete a {@code Movement}.
     *
     * @param pKey The identifying persistent key of the Movement to complete
     * @param vo Required data to set at completion
     * @return The completed instance
     */
    MovementVO complete(@NotEmpty String pKey, @NotNull MovementVO vo);
}
