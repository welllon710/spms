package com.spms.wms.model;

public record InputPageFilter(
        String billCode,
        Integer status,
        Integer type,
        Long purchaseId,
        Long moveId
) {
}
