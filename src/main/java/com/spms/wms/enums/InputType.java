package com.spms.wms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InputType {
    NORMAL(1, "普通入库"),
    MOVE(2, "移库入库"),
    PURCHASE(3, "采购入库"),
    PRODUCTION(4, "生产入库");

    private final Integer value;

    private final String name;
}
