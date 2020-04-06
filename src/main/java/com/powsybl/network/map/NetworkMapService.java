/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.map;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.map.model.LineMapData;
import com.powsybl.network.map.model.SubstationMapData;
import com.powsybl.network.map.model.VoltageLevelMapData;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@Service
class NetworkMapService {

    @Autowired
    private NetworkStoreService networkStoreService;

    private Network getNetwork(UUID networkUuid) {
        try {
            return networkStoreService.getNetwork(networkUuid, PreloadingStrategy.COLLECTION);
        } catch (PowsyblException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Network '" + networkUuid + "' not found");
        }
    }

    private static VoltageLevelMapData toMapData(VoltageLevel voltageLevel) {
        return VoltageLevelMapData.builder()
                .name(voltageLevel.getName())
                .id(voltageLevel.getId())
                .nominalVoltage(voltageLevel.getNominalV())
                .build();
    }

    private static SubstationMapData toMapData(Substation substation) {
        return SubstationMapData.builder()
                .name(substation.getName())
                .id(substation.getId())
                .countryName(substation.getCountry().map(Country::getName).orElse(null))
                .voltageLevels(substation.getVoltageLevelStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .build();
    }

    private static LineMapData toMapData(Line line) {
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineMapData.LineMapDataBuilder builder = LineMapData.builder()
                .name(line.getName())
                .id(line.getId())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId());
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1((int) Math.round(terminal1.getP()));
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1((int) Math.round(terminal1.getQ()));
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2((int) Math.round(terminal2.getP()));
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2((int) Math.round(terminal2.getQ()));
        }
        return builder.build();
    }

    public List<SubstationMapData> getSubstations(UUID networkUuid) {
        Network network = getNetwork(networkUuid);
        return network.getSubstationStream().map(NetworkMapService::toMapData).collect(Collectors.toList());
    }

    public List<LineMapData> getLines(UUID networkUuid) {
        Network network = getNetwork(networkUuid);
        return network.getLineStream().map(NetworkMapService::toMapData).collect(Collectors.toList());
    }
}
