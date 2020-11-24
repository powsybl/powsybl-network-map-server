/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import org.gridsuite.network.map.model.AllMapData;
import org.gridsuite.network.map.model.BatteryMapData;
import org.gridsuite.network.map.model.DanglingLineMapData;
import org.gridsuite.network.map.model.GeneratorMapData;
import org.gridsuite.network.map.model.HvdcLineMapData;
import org.gridsuite.network.map.model.LccConverterStationMapData;
import org.gridsuite.network.map.model.LineMapData;
import org.gridsuite.network.map.model.LoadMapData;
import org.gridsuite.network.map.model.ShuntCompensatorMapData;
import org.gridsuite.network.map.model.StaticVarCompensatorMapData;
import org.gridsuite.network.map.model.SubstationMapData;
import io.swagger.annotations.*;
import org.gridsuite.network.map.model.ThreeWindingsTransformerMapData;
import org.gridsuite.network.map.model.TwoWindingsTransformerMapData;
import org.gridsuite.network.map.model.VscConverterStationMapData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    @GetMapping(value = "/substations/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get substations description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Substations description")})
    public @ResponseBody List<SubstationMapData> getSubstations(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getSubstations(networkUuid, substationsIds);
    }

    @GetMapping(value = "/lines/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get lines description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Lines description")})
    public @ResponseBody List<LineMapData> getLines(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                    @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLines(networkUuid, substationsIds);
    }

    @GetMapping(value = "/generators/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get generators description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Generators description")})
    public @ResponseBody List<GeneratorMapData> getGenerators(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                              @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getGenerators(networkUuid, substationsIds);
    }

    @GetMapping(value = "/2-windings-transformers/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 2 windings transformers description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "2 windings transformers description")})
    public @ResponseBody List<TwoWindingsTransformerMapData> getTwoWindingsTransformers(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                        @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getTwoWindingsTransformers(networkUuid, substationsIds);
    }

    @GetMapping(value = "/3-windings-transformers/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 3 windings transformers description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "3 windings transformers description")})
    public @ResponseBody List<ThreeWindingsTransformerMapData> getThreeWindingsTransformers(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                            @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getThreeWindingsTransformers(networkUuid, substationsIds);
    }

    @GetMapping(value = "/all/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all equipments descriptions", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "all equipments descriptions")})
    public @ResponseBody AllMapData getAll(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                 @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getAll(networkUuid, substationsIds);
    }

    @GetMapping(value = "/batteries/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get batteries description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Batteries description")})
    public @ResponseBody List<BatteryMapData> getBatteries(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                           @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getBatteries(networkUuid, substationsIds);
    }

    @GetMapping(value = "/dangling-lines/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get dangling lines description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Dangling lines description")})
    public @ResponseBody List<DanglingLineMapData> getDanglingLines(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getDanglingLines(networkUuid, substationsIds);
    }

    @GetMapping(value = "/hvdc-lines/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get hvdc lines description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Hvdc lines description")})
    public @ResponseBody List<HvdcLineMapData> getHvdcLines(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getHvdcLines(networkUuid, substationsIds);
    }

    @GetMapping(value = "/lcc-converter-stations/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get lcc converter stations description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Lcc converter stations description")})
    public @ResponseBody List<LccConverterStationMapData> getLccConverterStations(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                       @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLccConverterStations(networkUuid, substationsIds);
    }

    @GetMapping(value = "/loads/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get loads description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Loads description")})
    public @ResponseBody List<LoadMapData> getLoads(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                    @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getLoads(networkUuid, substationsIds);
    }

    @GetMapping(value = "/shunt-compensators/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get shunt compensators description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Shunt compensators description")})
    public @ResponseBody List<ShuntCompensatorMapData> getShuntCompensators(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                 @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getShuntCompensators(networkUuid, substationsIds);
    }

    @GetMapping(value = "/static-var-compensators/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static var compensators description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Static var compensators description")})
    public @ResponseBody List<StaticVarCompensatorMapData> getStaticVarCompensators(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getStaticVarCompensators(networkUuid, substationsIds);
    }

    @GetMapping(value = "/vsc-converter-stations/{networkUuid}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get vsc converter stations description", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Vsc converter stations description")})
    public @ResponseBody List<VscConverterStationMapData> getVscConverterStations(@ApiParam(value = "Network UUID") @PathVariable("networkUuid") UUID networkUuid,
                                                                                  @ApiParam(value = "Substations id") @RequestParam(name = "substationId", required = false) List<String> substationsIds) {
        return networkMapService.getVscConverterStations(networkUuid, substationsIds);
    }
}
