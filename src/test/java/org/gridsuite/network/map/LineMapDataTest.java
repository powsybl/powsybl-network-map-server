package org.gridsuite.network.map;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.gridsuite.network.map.model.LineMapData;
import org.junit.Test;

public class LineMapDataTest {

    @Test
    public void equalsContract() {
        EqualsVerifier.simple().forClass(LineMapData.class).verify();
    }
}
