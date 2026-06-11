package com.spms.channel.model;

public record PurchasePricePageFilter(
        Long materialId,
        Long supplierId,
        String materialName,
        String materialCode,
        String supplierName,
        String supplierCode,
        Boolean isDisabled
) {
}
