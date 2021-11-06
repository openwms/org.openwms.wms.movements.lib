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
package org.openwms.wms;

import org.ameba.http.MeasuredRestController;
import org.openwms.core.http.AbstractWebController;
import org.openwms.core.http.Index;
import org.openwms.wms.api.MovementType;
import org.openwms.wms.api.MovementVO;
import org.openwms.wms.impl.ValidationGroups;
import org.openwms.wms.spi.DefaultMovementState;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

import static org.openwms.wms.api.MovementApi.API_MOVEMENTS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A MovementController.
 *
 * @author Heiko Scherrer
 */
@Validated
@MeasuredRestController
public class MovementController extends AbstractWebController {

    private final MovementService service;

    public MovementController(MovementService service) {
        this.service = service;
    }

    @GetMapping(API_MOVEMENTS + "/index")
    public ResponseEntity<Index> index() {
        return ResponseEntity.ok(
                new Index(
                        linkTo(methodOn(MovementController.class).create("transportUnitBK", new MovementVO(), null)).withRel("movement-create"),
                        linkTo(methodOn(MovementController.class).findAll()).withRel("movement-findAll"),
                        linkTo(methodOn(MovementController.class).findForStateAndTypesAndSource("state", "source", MovementType.INBOUND)).withRel("movement-findForStateAndTypesAndSource"),
                        linkTo(methodOn(MovementController.class).move("pKey", new MovementVO())).withRel("movement-move"),
                        linkTo(methodOn(MovementController.class).cancel("pKey")).withRel("movement-cancel"),
                        linkTo(methodOn(MovementController.class).complete("pKey", new MovementVO())).withRel("movement-complete")
                )
        );
    }

    @PostMapping("/v1/transport-units/{bk}/movements")
    @Validated(ValidationGroups.Movement.Create.class)
    public ResponseEntity<MovementVO> create(@PathVariable("bk") String bk, @Valid @RequestBody MovementVO movement,
                                       HttpServletRequest req) {

        movement.setTransportUnitBk(bk);
        MovementVO created = service.create(bk, movement);
        return ResponseEntity
                .created(getLocationURIForCreatedResource(req, created.getPersistentKey()))
                .body(created);
    }

    @PatchMapping(API_MOVEMENTS + "/{pKey}")
    @Validated(ValidationGroups.Movement.Move.class)
    public ResponseEntity<MovementVO> move(@PathVariable("pKey") String pKey,
            @Valid @RequestBody MovementVO movement) {

        return ResponseEntity.ok(service.move(pKey, movement));
    }

    @PatchMapping(API_MOVEMENTS + "/{pKey}/complete")
    @Validated(ValidationGroups.Movement.Complete.class)
    public ResponseEntity<MovementVO> complete(@PathVariable("pKey") String pKey,
                                               @Valid @RequestBody MovementVO movement) {

        return ResponseEntity.ok(service.complete(pKey, movement));
    }

    @DeleteMapping(API_MOVEMENTS + "/{pKey}")
    public ResponseEntity<MovementVO> cancel(@PathVariable("pKey") String pKey) {

        return ResponseEntity.ok(service.cancel(pKey));
    }

    @GetMapping(value = API_MOVEMENTS)
    public ResponseEntity<List<MovementVO>> findAll(){

        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(value = API_MOVEMENTS, params = {"barcode"})
    public ResponseEntity<List<MovementVO>> findForTU(@RequestParam("barcode") String barcode) {

        return ResponseEntity.ok(service.findForTU(barcode));
    }

    @GetMapping(value = API_MOVEMENTS, params = {"state", "types"})
    public ResponseEntity<List<MovementVO>> findForStateAndTypesAndSource(@RequestParam("state") String state,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam("types") MovementType... types) {

        return ResponseEntity.ok(service.findFor(DefaultMovementState.valueOf(state), source, types));
    }
}
