/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.map.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Builder
@Getter
public class LineMapData {

    private String id;

    private String voltageLevelId1;

    private String voltageLevelId2;

    private String name;

    private Boolean isTerminal1Connected;

    private Boolean isTerminal2Connected;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer p1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer q1;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer p2;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer q2;
}
