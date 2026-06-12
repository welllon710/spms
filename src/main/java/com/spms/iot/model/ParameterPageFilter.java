package com.spms.iot.model;

public record ParameterPageFilter(
        String code,
        String label,
        Boolean isSystem,
        Integer dataType,
        Boolean isDisabled
) {
}
