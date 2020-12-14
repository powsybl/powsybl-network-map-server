package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.TwoWindingsTransformerMapData;
import org.junit.Test;

public class TwoWindingsTransformerMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(TwoWindingsTransformerMapData.class).verify();
    }
}
