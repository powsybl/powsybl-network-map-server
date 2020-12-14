package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.SubstationMapData;
import org.junit.Test;

public class SubstationMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(SubstationMapData.class).verify();
    }
}
