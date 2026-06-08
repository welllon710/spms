package com.spms.personal.model;

import com.spms.base.PageParam;
import com.spms.base.PageQueryRequest;

public record MenuPageRequest(
        MenuPageFilter filter,
        PageParam page
) implements PageQueryRequest<MenuPageFilter> {
}
