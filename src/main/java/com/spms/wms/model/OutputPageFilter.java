package com.spms.wms.model;

public record OutputPageFilter(
        String billCode,
        Integer status,
        Integer type,
        Long saleId,
        Long moveId,
        Long pickingId
) {
}
