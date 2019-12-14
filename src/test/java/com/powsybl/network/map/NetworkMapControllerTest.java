/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.map;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
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

        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.COLLECTION)).willReturn(EurostagTutorialExample1Factory.create());
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
                .andExpect(content().json(resourceToString("/substations-map-data.json")));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfSubstationsMapData() throws Exception {
        mvc.perform(get("/v1/substations/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturnLinesMapData() throws Exception {
        mvc.perform(get("/v1/lines/{networkUuid}/", NETWORK_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(resourceToString("/lines-map-data.json")));
    }

    @Test
    public void shouldReturnAnErrorInsteadOfLinesMapData() throws Exception {
        mvc.perform(get("/v1/lines/{networkUuid}/", NOT_FOUND_NETWORK_ID))
                .andExpect(status().isNoContent());
    }
}
