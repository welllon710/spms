package com.spms.asset.model;

public record DevicePageFilter(
        String name,
        String code,
        String uuid,
        Integer status,
        Integer alarm,
        Boolean isReporting,
        Boolean isDisabled
) {
}
