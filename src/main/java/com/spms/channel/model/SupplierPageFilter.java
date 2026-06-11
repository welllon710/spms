package com.spms.channel.model;

public record SupplierPageFilter(
        String name,
        String code,
        String phone,
        Boolean isDisabled
) {
}
