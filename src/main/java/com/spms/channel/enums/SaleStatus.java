package com.spms.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SaleStatus {
    AUDITING(1, "审核中"),
    REJECTED(2, "已驳回"),
    OUT_STORAGE(3, "出库中"),
    FINISHED(4, "已完成");

    private final Integer value;

    private final String name;
}
