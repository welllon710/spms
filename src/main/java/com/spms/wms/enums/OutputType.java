package com.spms.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutputType {
    NORMAL(1, "普通出库"),
    MOVE(2, "移库出库"),
    SALE(3, "销售出库"),
    PICKING(4, "领料出库");

    private final Integer value;

    private final String name;
}
