/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Builder
@Getter
public class AllMapData {
    private List<SubstationMapData> substations;
    private List<LineMapData> lines;
    private List<TwoWindingsTransformerMapData> twoWindingsTransformers;
    private List<ThreeWindingsTransformerMapData> threeWindingsTransformers;
    private List<GeneratorMapData> generators;
    private List<BatteryMapData> batteries;
    private List<DanglingLineMapData> danglingLines;
    private List<HvdcLineMapData> hvdcLines;
    private List<LccConverterStationMapData> lccConverterStations;
    private List<LoadMapData> loads;
    private List<ShuntCompensatorMapData> shuntCompensators;
    private List<StaticVarCompensatorMapData> staticVarCompensators;
    private List<VscConverterStationMapData> vscConverterStations;
}
