package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.VoltageLevelMapData;
import org.junit.Test;

public class VoltageLevelMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(VoltageLevelMapData.class).verify();
    }
}
