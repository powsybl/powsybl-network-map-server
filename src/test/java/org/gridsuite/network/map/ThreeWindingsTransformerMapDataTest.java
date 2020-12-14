package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.ThreeWindingsTransformerMapData;
import org.junit.Test;

public class ThreeWindingsTransformerMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(ThreeWindingsTransformerMapData.class).verify();
    }
}
