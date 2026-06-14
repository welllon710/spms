package com.spms.system.enums;

import lombok.Getter;

@Getter
public enum CodeRuleField {
    SUPPLIER_CODE(2, "供应商编码", "SUP", SerialNumberUpdate.YEAR),
    CUSTOMER_CODE(5, "客户编码", "CT", SerialNumberUpdate.YEAR),
    DEVICE_CODE(6, "设备编码", "DV", SerialNumberUpdate.YEAR),
    PURCHASE_BILL_CODE(8, "采购单号", "PC", SerialNumberUpdate.DAY),
    SALE_BILL_CODE(9, "销售单号", "SL", SerialNumberUpdate.DAY),
    INPUT_BILL_CODE(10, "入库单号", "IN", SerialNumberUpdate.DAY),
    OUTPUT_BILL_CODE(11, "出库单号", "OUT", SerialNumberUpdate.DAY),
    MOVE_BILL_CODE(12, "移库单号", "MV", SerialNumberUpdate.DAY),
    STORAGE_CODE(13, "仓库编码", "ST", SerialNumberUpdate.YEAR),
    MATERIAL_CODE(14, "物料编码", "MT", SerialNumberUpdate.YEAR),
    UNIT_CODE(15, "单位编码", "UN", SerialNumberUpdate.YEAR);

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
