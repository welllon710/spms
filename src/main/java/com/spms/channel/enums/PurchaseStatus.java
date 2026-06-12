package com.spms.channel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PurchaseStatus {

    AUDITING(1, "审核中"),
    REJECTED(2, "已驳回"),
    PURCHASING(3, "采购中"),
    IN_STORAGE(4, "入库中"),
    FINISHED(5, "已完成");

    private final Integer value;

    private final String name;
}