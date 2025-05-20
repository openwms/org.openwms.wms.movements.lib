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
package org.openwms.wms.movements.ui;

import org.ameba.http.MeasuredRestController;
import org.openwms.core.http.AbstractWebController;
import org.openwms.wms.movements.MovementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * A MovementUIController.
 *
 * @author Heiko Scherrer
 */
@Validated
@MeasuredRestController
public class MovementUIController extends AbstractWebController {

    private final MovementService service;

    public MovementUIController(MovementService service) {
        this.service = service;
    }

    @GetMapping(value = "/v1/movements/priorities")
    public ResponseEntity<List<String>> getPriorityList() {
        return new ResponseEntity<>(this.service.getPriorityList(), HttpStatus.OK);
    }
}
