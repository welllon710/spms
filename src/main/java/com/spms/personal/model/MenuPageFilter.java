package com.spms.personal.model;

public record MenuPageFilter(
        String name,
        Long parentId,
        String path,
        String component,
        Boolean isDisabled
) {
}
