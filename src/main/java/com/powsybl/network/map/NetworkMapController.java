/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.map;

import com.powsybl.network.map.model.LineMapData;
import com.powsybl.network.map.model.SubstationMapData;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
}
