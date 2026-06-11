package com.spms.asset.model;

public record MaterialPageFilter(
        String name,
        String code,
        String spc,
        Long materialType,
        Long useType,
        Long unitId,
        Boolean isDisabled
) {
}
