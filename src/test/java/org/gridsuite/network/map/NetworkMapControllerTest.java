/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;
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

        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.COLLECTION)).willReturn(network);
        given(networkStoreService.getNetwork(NOT_FOUND_NETWORK_ID, PreloadingStrategy.COLLECTION)).willThrow(new PowsyblException("Network " + NOT_FOUND_NETWORK_ID + " not found"));
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
        mvc.perform(get("/v1/substations/{networkUuid}/", NOT_FOUND_NETWORK_ID))
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
}
