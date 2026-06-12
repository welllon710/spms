package com.spms.system.enums;

import lombok.Getter;

@Getter
public enum CodeRuleField {
    SUPPLIER_CODE(2, "供应商编码", "SUP", SerialNumberUpdate.YEAR),
    CUSTOMER_CODE(5, "客户编码", "CT", SerialNumberUpdate.YEAR),
    PURCHASE_BILL_CODE(8, "采购单号", "PC", SerialNumberUpdate.DAY),
    SALE_BILL_CODE(9, "销售单号", "SL", SerialNumberUpdate.DAY);

    private final int key;
    private final String name;
    private final String defaultPrefix;
    private final SerialNumberUpdate defaultSnType;

    CodeRuleField(int key, String name, String defaultPrefix, SerialNumberUpdate defaultSnType) {
        this.key = key;
        this.name = name;
        this.defaultPrefix = defaultPrefix;
        this.defaultSnType = defaultSnType;
    }
}
