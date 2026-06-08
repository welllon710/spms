package com.spms.base;

public interface PageQueryRequest<T> extends PageRequest {
    T filter();

    PageParam page();

    @Override
    default Integer pageNum() {
        return page() == null ? null : page().pageNum();
    }

    @Override
    default Integer pageSize() {
        return page() == null ? null : page().pageSize();
    }
}
