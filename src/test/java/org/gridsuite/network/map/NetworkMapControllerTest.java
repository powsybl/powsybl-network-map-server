/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(NetworkMapController.class)
@ContextConfiguration(classes = {NetworkMapApplication.class})
public class NetworkMapControllerTest {

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");

    private static final UUID NOT_FOUND_NETWORK_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private  NetworkStoreService networkStoreService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Network network = EurostagTutorialExample1Factory.create();
        Line l1 = network.getLine("NHV1_NHV2_1");
        l1.getTerminal1().setP(1.1)
                .setQ(2.2);
        l1.getTerminal2().setP(3.33)
                .setQ(4.44);
        l1.newCurrentLimits1().setPermanentLimit(700.4).add();
        l1.newCurrentLimits2().setPermanentLimit(800.8).add();
        network.getSubstation("P2").setCountry(null);

        TwoWindingsTransformer t1 = network.getTwoWindingsTransformer("NHV2_NLOAD");
        t1.getTerminal1().setP(5.5)
                .setQ(6.6);
        t1.getTerminal2().setP(7.77)
                .setQ(8.88);
        t1.newCurrentLimits1().setPermanentLimit(900.5).add();
        t1.newCurrentLimits2().setPermanentLimit(950.5).add();
        t1.getRatioTapChanger().setTapPosition(2);

        TwoWindingsTransformer t2 = network.getTwoWindingsTransformer("NGEN_NHV1");
        t2.getTerminal1().setP(11.1)
                .setQ(12.2);
        t2.getTerminal2().setP(13.33)
                .setQ(14.44);
        t2.newCurrentLimits1().setPermanentLimit(750.4).add();
        t2.newCurrentLimits2().setPermanentLimit(780.6).add();
        t2.newPhaseTapChanger()
                .beginStep()
                .setAlpha(1)
                .setRho(0.85f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(1)
                .setRho(0.90f)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .setTapPosition(1)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(10)
                .setRegulationTerminal(t2.getTerminal1())
                .setTargetDeadband(0)
                .add();

        Generator gen = network.getGenerator("GEN");
        gen.getTerminal().setP(25);
        gen.getTerminal().setQ(32);
        gen.setTargetP(28);

        Substation p1 = network.getSubstation("P1");
        VoltageLevel vlnew2 = p1.newVoltageLevel()
                .setId("VLNEW2")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vlnew2.getBusBreakerView().newBus()
                .setId("NNEW2")
            .add();

        ThreeWindingsTransformer threeWindingsTransformer = p1.newThreeWindingsTransformer()
                .setId("TWT")
                .setName("TWT")
                .setRatedU0(234)
                .newLeg1()
                .setVoltageLevel("VLHV1")
                .setBus("NHV1")
                .setR(45)
                .setX(35)
                .setG(25)
                .setB(15)
                .setRatedU(5)
                .add()
                .newLeg2()
                .setVoltageLevel("VLNEW2")
                .setBus("NNEW2")
                .setR(47)
                .setX(37)
                .setG(27)
                .setB(17)
                .setRatedU(7)
                .add()
                .newLeg3()
                .setVoltageLevel("VLGEN")
                .setBus("NGEN")
                .setR(49)
                .setX(39)
                .setG(29)
                .setB(19)
                .setRatedU(9)
                .add()
                .add();
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).setP(375);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).setP(225);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).setP(200);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).setQ(48);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).setQ(28);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).setQ(18);

        threeWindingsTransformer.getLeg1().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(25)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE))
                .setTargetDeadband(22)
                .beginStep()
                .setAlpha(-10)
                .setRho(0.99)
                .setR(1.)
                .setX(4.)
                .setG(0.5)
                .setB(1.5)
                .endStep()
                .beginStep()
                .setAlpha(0)
                .setRho(1)
                .setR(1.1)
                .setX(4.1)
                .setG(0.6)
                .setB(1.6)
                .endStep()
                .beginStep()
                .setAlpha(10)
                .setRho(1.01)
                .setR(1.2)
                .setX(4.2)
                .setG(0.7)
                .setB(1.7)
                .endStep()
                .add();
        threeWindingsTransformer.getLeg2().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(2)
                .setRegulating(false)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE))
                .setTargetDeadband(22)
                .setTargetV(220)
                .beginStep()
                .setRho(0.99)
                .setR(1.)
                .setX(4.)
                .setG(0.5)
                .setB(1.5)
                .endStep()
                .beginStep()
                .setRho(1)
                .setR(1.1)
                .setX(4.1)
                .setG(0.6)
                .setB(1.6)
                .endStep()
                .beginStep()
                .setRho(1.01)
                .setR(1.2)
                .setX(4.2)
                .setG(0.7)
                .setB(1.7)
                .endStep()
                .add();

        threeWindingsTransformer.getLeg1()
                .newCurrentLimits()
                .setPermanentLimit(25)
                .add();
        threeWindingsTransformer.getLeg3()
                .newCurrentLimits()
                .setPermanentLimit(54)
                .add();

        Substation p3 = network.newSubstation()
                .setId("P3")
                .setCountry(Country.FR)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
        VoltageLevel vlgen3 = p3.newVoltageLevel()
                .setId("VLGEN3")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vlgen3.getBusBreakerView().newBus()
                .setId("NGEN3")
                .add();
        network.newLine()
                .setId("LINE3")
                .setVoltageLevel1("VLGEN")
                .setBus1("NGEN")
                .setConnectableBus1("NGEN")
                .setVoltageLevel2("VLGEN3")
                .setBus2("NGEN3")
                .setConnectableBus2("NGEN3")
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
            .add();

        Battery b1 = vlnew2.newBattery()
                .setId("BATTERY1")
                .setName("BATTERY1")
                .setMinP(0)
                .setMaxP(10)
                .setP0(1)
                .setQ0(1)
                .setConnectableBus("NNEW2")
                .add();
        b1.getTerminal().setP(50);
        b1.getTerminal().setQ(70);

        vlgen3.newBattery()
                .setId("BATTERY2")
                .setName("BATTERY2")
                .setMinP(0)
                .setMaxP(10)
                .setP0(1)
                .setQ0(1)
                .setConnectableBus("NGEN3")
                .add();

        VoltageLevel vl1 = network.getVoltageLevel("VLGEN");
        DanglingLine dl = vl1.newDanglingLine()
                .setId("DL1")
                .setName("DL1")
                .setR(1)
                .setX(2)
                .setB(3)
                .setG(4)
                .setP0(50)
                .setQ0(30)
                .setUcteXnodeCode("xnode1")
                .setConnectableBus("NGEN")
                .setBus("NGEN")
                .add();
        dl.getTerminal().setP(45);
        dl.getTerminal().setQ(75);

        vlgen3.newDanglingLine()
                .setId("DL2")
                .setName("DL2")
                .setR(1)
                .setX(2)
                .setB(3)
                .setG(4)
                .setP0(50)
                .setQ0(30)
                .setUcteXnodeCode("xnode1")
                .setConnectableBus("NGEN3")
                .setBus("NGEN3")
                .add();

        VscConverterStation vsc1 = vlnew2.newVscConverterStation()
                .setId("VSC1")
                .setName("VSC1")
                .setLossFactor(1)
                .setReactivePowerSetpoint(40)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(150)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        vsc1.getTerminal().setP(10);
        vsc1.getTerminal().setQ(30);

        vlgen3.newVscConverterStation()
                .setId("VSC2")
                .setName("VSC2")
                .setLossFactor(1)
                .setReactivePowerSetpoint(40)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(150)
                .setConnectableBus("NGEN3")
                .setBus("NGEN3")
                .add();

        vl1.newLccConverterStation()
                .setId("LCC1")
                .setName("LCC1")
                .setLossFactor(1)
                .setPowerFactor(0.5F)
                .setConnectableBus("NGEN")
                .setBus("NGEN")
                .add();

        LccConverterStation lcc2 = vlnew2.newLccConverterStation()
                .setId("LCC2")
                .setName("LCC2")
                .setLossFactor(1)
                .setPowerFactor(0.5F)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        lcc2.getTerminal().setP(110);
        lcc2.getTerminal().setQ(310);

        network.newHvdcLine()
                .setId("HVDC1")
                .setName("HVDC1")
                .setR(1)
                .setMaxP(100)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(225)
                .setActivePowerSetpoint(500)
                .setConverterStationId1("VSC1")
                .setConverterStationId2("LCC2")
                .add();

        ShuntCompensator shunt1 = vlnew2.newShuntCompensator()
                .setId("SHUNT1")
                .setName("SHUNT1")
                .newLinearModel()
                .setMaximumSectionCount(3)
                .setBPerSection(1)
                .setGPerSection(2)
                .add()
                .setSectionCount(2)
                .setTargetV(225)
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();
        shunt1.getTerminal().setQ(90);

        vlgen3.newShuntCompensator()
                .setId("SHUNT2")
                .setName("SHUNT2")
                .newLinearModel()
                .setMaximumSectionCount(3)
                .setBPerSection(1)
                .setGPerSection(2)
                .add()
                .setSectionCount(2)
                .setTargetV(225)
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setConnectableBus("NGEN3")
                .setBus("NGEN3")
                .add();

        StaticVarCompensator svc1 = vl1.newStaticVarCompensator()
                .setId("SVC1")
                .setName("SVC1")
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(200)
                .setReactivePowerSetpoint(100)
                .setBmin(2)
                .setBmax(30)
                .setConnectableBus("NGEN")
                .setBus("NGEN")
                .add();
        svc1.getTerminal().setP(120);
        svc1.getTerminal().setQ(43);

        vlnew2.newStaticVarCompensator()
                .setId("SVC2")
                .setName("SVC2")
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(200)
                .setReactivePowerSetpoint(100)
                .setBmin(2)
                .setBmax(30)
                .setConnectableBus("NNEW2")
                .setBus("NNEW2")
                .add();

        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.COLLECTION)).willReturn(network);
        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.NONE)).willReturn(network);
        given(networkStoreService.getNetwork(NOT_FOUND_NETWORK_ID, PreloadingStrategy.COLLECTION)).willThrow(new PowsyblException("Network " + NOT_FOUND_NETWORK_ID + " not found"));
        given(networkStoreService.getNetwork(NOT_FOUND_NETWORK_ID, PreloadingStrategy.NONE)).willThrow(new PowsyblException("Network " + NOT_FOUND_NETWORK_ID + " not found"));
    }

    private String resourceToString(String resource) throws IOException {
        return new String(ByteStreams.toByteArray(getClass().getResourceAsStream(resource)), StandardCharsets.UTF_8);
    }

    @Test
    public void shouldReturnSubstationsMapData() throws Exception {
        mvc.perform(get("/v1/substations/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/substations-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapData() throws Exception {
        mvc.perform(get("/v1/substations/{networkUuid}", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnSubstationsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/substations/{networkUuid}?substationId=P1", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-substations-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/substations/{networkUuid}?substationId=P1&substationId=P2", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnLinesMapData() throws Exception {
        mvc.perform(get("/v1/lines/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/lines-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapData() throws Exception {
        mvc.perform(get("/v1/lines/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnLinesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/lines/{networkUuid}?substationId=P3", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-lines-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/lines/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnGeneratorsMapData() throws Exception {
        mvc.perform(get("/v1/generators/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/generators-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapData() throws Exception {
        mvc.perform(get("/v1/generators/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnGeneratorsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/generators/{networkUuid}?substationId=P2", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfGeneratorsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/generators/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnTwoWindingsTransformersMapData() throws Exception {
        mvc.perform(get("/v1/2-windings-transformers/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/2-windings-transformers-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapData() throws Exception {
        mvc.perform(get("/v1/2-windings-transformers/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnTwoWindingsTransformersMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/2-windings-transformers/{networkUuid}?substationId=P1", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-2-windings-transformers-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfTwoWindingsTransformersMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/2-windings-transformers/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnThreeWindingsTransformersMapData() throws Exception {
        mvc.perform(get("/v1/3-windings-transformers/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/3-windings-transformers-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapData() throws Exception {
        mvc.perform(get("/v1/3-windings-transformers/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnThreeWindingsTransformersMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/3-windings-transformers/{networkUuid}?substationId=P3", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfThreeWindingsTransformersMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/3-windings-transformers/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnAllMapData() throws Exception {
        mvc.perform(get("/v1/all/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/all-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapData() throws Exception {
        mvc.perform(get("/v1/all/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnAllMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/all/{networkUuid}?substationId=P3", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-all-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfAllMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/all/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnBatteriesMapData() throws Exception {
        mvc.perform(get("/v1/batteries/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/batteries-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapData() throws Exception {
        mvc.perform(get("/v1/batteries/{networkUuid}", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnBatteriesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/batteries/{networkUuid}?substationId=P1", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-batteries-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfBatteriesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/batteries/{networkUuid}?substationId=P1&substationId=P2", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnDanglingLinesMapData() throws Exception {
        mvc.perform(get("/v1/dangling-lines/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/dangling-lines-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapData() throws Exception {
        mvc.perform(get("/v1/dangling-lines/{networkUuid}", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnDanglingLinesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/dangling-lines/{networkUuid}?substationId=P2", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfDanglingLinesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/dangling-lines/{networkUuid}?substationId=P1&substationId=P2", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnLoadsMapData() throws Exception {
        mvc.perform(get("/v1/loads/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/loads-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapData() throws Exception {
        mvc.perform(get("/v1/loads/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnLoadsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/loads/{networkUuid}?substationId=P2", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-loads-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLoadsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/loads/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnShuntCompensatorsMapData() throws Exception {
        mvc.perform(get("/v1/shunt-compensators/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/shunt-compensators-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapData() throws Exception {
        mvc.perform(get("/v1/shunt-compensators/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnShuntCompensatorsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/shunt-compensators/{networkUuid}?substationId=P1", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-shunt-compensators-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfShuntCompensatorsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/shunt-compensators/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnStaticVarCompensatorsMapData() throws Exception {
        mvc.perform(get("/v1/static-var-compensators/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/static-var-compensators-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapData() throws Exception {
        mvc.perform(get("/v1/static-var-compensators/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnStaticVarCompensatorsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/static-var-compensators/{networkUuid}?substationId=P1", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-static-var-compensators-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfStaticVarCompensatorsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/static-var-compensators/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnLccConverterStationsMapData() throws Exception {
        mvc.perform(get("/v1/lcc-converter-stations/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/lcc-converter-stations-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapData() throws Exception {
        mvc.perform(get("/v1/lcc-converter-stations/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnLccConverterStationsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/lcc-converter-stations/{networkUuid}?substationId=P1", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-lcc-converter-stations-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLccConverterStationsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/lcc-converter-stations/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnVscConverterStationsMapData() throws Exception {
        mvc.perform(get("/v1/vsc-converter-stations/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/vsc-converter-stations-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapData() throws Exception {
        mvc.perform(get("/v1/vsc-converter-stations/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnVscConverterStationsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/vsc-converter-stations/{networkUuid}?substationId=P1", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/partial-vsc-converter-stations-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfVscConverterStationsMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/vsc-converter-stations/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnHvdcLinesMapData() throws Exception {
        mvc.perform(get("/v1/hvdc-lines/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/hvdc-lines-map-data.json"), true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapData() throws Exception {
        mvc.perform(get("/v1/hvdc-lines/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnHvdcLinesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/hvdc-lines/{networkUuid}?substationId=P3", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]", true));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfHvdcLinesMapDataFromIds() throws Exception {
        mvc.perform(get("/v1/hvdc-lines/{networkUuid}?substationId=P1", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNotFound());
    }
}
