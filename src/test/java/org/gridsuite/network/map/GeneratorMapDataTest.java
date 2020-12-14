package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.GeneratorMapData;
import org.junit.Test;

public class GeneratorMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(GeneratorMapData.class).verify();
    }
}
