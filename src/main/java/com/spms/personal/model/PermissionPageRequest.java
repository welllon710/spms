package com.spms.personal.model;

import com.spms.base.PageParam;
import com.spms.base.PageQueryRequest;

public record PermissionPageRequest(
        PermissionPageFilter filter,
        PageParam page
) implements PageQueryRequest<PermissionPageFilter> {
}
