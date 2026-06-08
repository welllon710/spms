package com.spms.personal.model;

import com.spms.base.PageParam;
import com.spms.base.PageQueryRequest;

public record UnitPageRequest(
        UnitPageFilter filter,
        PageParam page
) implements PageQueryRequest<UnitPageFilter> {
}
