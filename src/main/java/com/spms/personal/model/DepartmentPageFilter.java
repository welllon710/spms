package com.spms.personal.model;

public record DepartmentPageFilter(
        String name,
        String code,
        Long parentId,
        Boolean isDisabled
) {
}
