/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.network.map.model.GeneratorMapData;
import org.gridsuite.network.map.model.LineMapData;
import org.gridsuite.network.map.model.SubstationMapData;
import org.gridsuite.network.map.model.ThreeWindingsTransformerMapData;
import org.gridsuite.network.map.model.TwoWindingsTransformerMapData;
import org.gridsuite.network.map.model.VoltageLevelMapData;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@Service
class NetworkMapService {

    @Autowired
    private NetworkStoreService networkStoreService;

    private Network getNetwork(UUID networkUuid, PreloadingStrategy strategy) {
        try {
            return networkStoreService.getNetwork(networkUuid, strategy);
        } catch (PowsyblException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Network '" + networkUuid + "' not found");
        }
    }

    private static VoltageLevelMapData toMapData(VoltageLevel voltageLevel) {
        return VoltageLevelMapData.builder()
                .name(voltageLevel.getNameOrId())
                .id(voltageLevel.getId())
                .nominalVoltage(voltageLevel.getNominalV())
                .build();
    }

    private static SubstationMapData toMapData(Substation substation) {
        return SubstationMapData.builder()
                .name(substation.getNameOrId())
                .id(substation.getId())
                .countryName(substation.getCountry().map(Country::getName).orElse(null))
                .voltageLevels(substation.getVoltageLevelStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .build();
    }

    private static LineMapData toMapData(Line line) {
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineMapData.LineMapDataBuilder builder = LineMapData.builder()
                .name(line.getNameOrId())
                .id(line.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId());
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        CurrentLimits limits1 = line.getCurrentLimits1();
        CurrentLimits limits2 = line.getCurrentLimits2();

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        return builder.build();
    }

    private static GeneratorMapData toMapData(Generator generator) {
        Terminal terminal = generator.getTerminal();
        GeneratorMapData.GeneratorMapDataBuilder builder = GeneratorMapData.builder()
                .name(generator.getNameOrId())
                .id(generator.getId())
                .terminalConnected(terminal.isConnected())
                .voltageLevelId(terminal.getVoltageLevel().getId());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(generator.getTargetP())) {
            builder.targetP(generator.getTargetP());
        }
        return builder.build();
    }

    private static TwoWindingsTransformerMapData toMapData(TwoWindingsTransformer transformer) {
        Terminal terminal1 = transformer.getTerminal1();
        Terminal terminal2 = transformer.getTerminal2();
        TwoWindingsTransformerMapData.TwoWindingsTransformerMapDataBuilder builder = TwoWindingsTransformerMapData.builder()
                .name(transformer.getNameOrId())
                .id(transformer.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId());
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        CurrentLimits limits1 = transformer.getCurrentLimits1();
        CurrentLimits limits2 = transformer.getCurrentLimits2();

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }

        if (transformer.hasRatioTapChanger()) {
            builder.ratioTapChangerPosition(transformer.getRatioTapChanger().getTapPosition());
        }
        if (transformer.hasPhaseTapChanger()) {
            builder.phaseTapChangerPosition(transformer.getPhaseTapChanger().getTapPosition());
        }
        return builder.build();
    }

    private static ThreeWindingsTransformerMapData toMapData(ThreeWindingsTransformer transformer) {
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        Terminal terminal1 = leg1.getTerminal();
        Terminal terminal2 = leg2.getTerminal();
        Terminal terminal3 = leg3.getTerminal();

        ThreeWindingsTransformerMapData.ThreeWindingsTransformerMapDataBuilder builder = ThreeWindingsTransformerMapData.builder()
                .name(transformer.getNameOrId())
                .id(transformer.getId())
                .terminal1Connected(terminal1.isConnected())
                .terminal2Connected(terminal2.isConnected())
                .terminal3Connected(terminal3.isConnected())
                .voltageLevelId1(terminal1.getVoltageLevel().getId())
                .voltageLevelId2(terminal2.getVoltageLevel().getId())
                .voltageLevelId3(terminal3.getVoltageLevel().getId());
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal3.getP())) {
            builder.p3(terminal3.getP());
        }
        if (!Double.isNaN(terminal3.getQ())) {
            builder.q3(terminal3.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        if (!Double.isNaN(terminal3.getI())) {
            builder.i3(terminal3.getI());
        }

        CurrentLimits limits1 = leg1.getCurrentLimits();
        CurrentLimits limits2 = leg2.getCurrentLimits();
        CurrentLimits limits3 = leg3.getCurrentLimits();

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        if (limits3 != null && !Double.isNaN(limits3.getPermanentLimit())) {
            builder.permanentLimit3(limits3.getPermanentLimit());
        }

        if (leg1.hasRatioTapChanger()) {
            builder.ratioTapChanger1Position(leg1.getRatioTapChanger().getTapPosition());
        }
        if (leg2.hasRatioTapChanger()) {
            builder.ratioTapChanger2Position(leg2.getRatioTapChanger().getTapPosition());
        }
        if (leg3.hasRatioTapChanger()) {
            builder.ratioTapChanger3Position(leg3.getRatioTapChanger().getTapPosition());
        }

        if (leg1.hasPhaseTapChanger()) {
            builder.phaseTapChanger1Position(leg1.getPhaseTapChanger().getTapPosition());
        }
        if (leg2.hasPhaseTapChanger()) {
            builder.phaseTapChanger2Position(leg2.getPhaseTapChanger().getTapPosition());
        }
        if (leg3.hasPhaseTapChanger()) {
            builder.phaseTapChanger3Position(leg3.getPhaseTapChanger().getTapPosition());
        }

        return builder.build();
    }

    public List<SubstationMapData> getSubstations(UUID networkUuid, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE);
        if (substationsId == null) {
            return network.getSubstationStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            List<SubstationMapData> res = new ArrayList<>();
            substationsId.stream().forEach(id -> res.add(toMapData(network.getSubstation(id))));
            return res;
        }
    }

    public List<LineMapData> getLines(UUID networkUuid, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE);
        if (substationsId == null) {
            return network.getLineStream()
                    .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<LineMapData> res = new HashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Line.class).forEach(l -> res.add(toMapData(l)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<GeneratorMapData> getGenerators(UUID networkUuid, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE);
        if (substationsId == null) {
            return network.getGeneratorStream()
                    .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<GeneratorMapData> res = new HashSet<>();
            substationsId.stream().forEach(id ->
                    network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                            v.getConnectables(Generator.class).forEach(g -> res.add(toMapData(g)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<TwoWindingsTransformerMapData> getTwoWindingsTransformers(UUID networkUuid, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE);
        if (substationsId == null) {
            return network.getTwoWindingsTransformerStream()
                    .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<TwoWindingsTransformerMapData> res = new HashSet<>();
            substationsId.stream().forEach(id ->
                    network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                            v.getConnectables(TwoWindingsTransformer.class).forEach(t -> res.add(toMapData(t)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<ThreeWindingsTransformerMapData> getThreeWindingsTransformers(UUID networkUuid, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE);
        if (substationsId == null) {
            return network.getThreeWindingsTransformerStream()
                    .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<ThreeWindingsTransformerMapData> res = new HashSet<>();
            substationsId.stream().forEach(id ->
                    network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                            v.getConnectables(ThreeWindingsTransformer.class).forEach(t -> res.add(toMapData(t)))));
            return res.stream().collect(Collectors.toList());
        }
    }
}
