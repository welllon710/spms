package com.spms.personal.model;

import com.spms.base.PageParam;
import com.spms.base.PageQueryRequest;

public record DepartmentPageRequest(
        DepartmentPageFilter filter,
        PageParam page
) implements PageQueryRequest<DepartmentPageFilter> {
}
