package com.spms.personal.model;

import com.spms.base.PageRequest;

public record RolePageRequest(
        Integer pageNum,
        Integer pageSize,
        String name,
        String code,
        Boolean isDisabled
) implements PageRequest {
}
