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

import org.ameba.http.MeasuredRestController;
import org.openwms.core.http.AbstractWebController;
import org.openwms.wms.api.MovementVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * A MovementController.
 *
 * @author Heiko Scherrer
 */
@MeasuredRestController
public class MovementController extends AbstractWebController {

    private final MovementService service;

    public MovementController(MovementService service) {
        this.service = service;
    }

    @PostMapping("/v1/transport-units/{bk}/movements")
    public ResponseEntity<Void> create(@PathVariable("bk") String bk, MovementVO movement, HttpServletRequest req) {
        MovementVO created = service.create(bk, movement);
        return ResponseEntity.created(getLocationURIForCreatedResource(req, created.getPersistentKey())).build();
    }
}
