/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import org.gridsuite.network.map.model.GeneratorMapData;
import org.gridsuite.network.map.model.LineMapData;
import org.gridsuite.network.map.model.SubstationMapData;
import io.swagger.annotations.*;
import org.gridsuite.network.map.model.ThreeWindingsTransformerMapData;
import org.gridsuite.network.map.model.TwoWindingsTransformerMapData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + NetworkMapController.API_VERSION + "/")
@Api(tags = "network-map-server")
@ComponentScan(basePackageClasses = NetworkMapService.class)
public class NetworkMapController {

    public static final String API_VERSION = "v1";

    @Autowired
    private NetworkMapService networkMapService;

    @GetMapping(value = "/substations/{networkUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get substations description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Substations description")})
    public @ResponseBody List<SubstationMapData> getSubstations(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid) {
        return networkMapService.getSubstations(networkUuid);
    }

    @GetMapping(value = "/lines/{networkUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get lines description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Lines description")})
    public @ResponseBody List<LineMapData> getLines(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid) {
        return networkMapService.getLines(networkUuid);
    }

    @GetMapping(value = "/generators/{networkUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get generators description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Generators description")})
    public @ResponseBody List<GeneratorMapData> getGenerators(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid) {
        return networkMapService.getGenerators(networkUuid);
    }

    @GetMapping(value = "/2-windings-transformers/{networkUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 2 windings transformers description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "2 windings transformers description")})
    public @ResponseBody List<TwoWindingsTransformerMapData> getTwoWindingsTransformers(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid) {
        return networkMapService.getTwoWindingsTransformers(networkUuid);
    }

    @GetMapping(value = "/3-windings-transformers/{networkUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 3 windings transformers description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "3 windings transformers description")})
    public @ResponseBody List<ThreeWindingsTransformerMapData> getThreeWindingsTransformers(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid) {
        return networkMapService.getThreeWindingsTransformers(networkUuid);
    }
}
