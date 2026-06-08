package com.spms.personal.model;

public record PermissionPageFilter(
        String identity,
        String name,
        Long parentId,
        Integer type,
        Boolean isSystem,
        Boolean isDisabled
) {
}
