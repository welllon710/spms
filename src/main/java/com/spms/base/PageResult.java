package com.spms.base;

import com.github.pagehelper.PageInfo;

import java.util.List;

public record PageResult<T>(
        long total,
        int pageCount,
        List<T> list,
        PageParam page,
        SortParam sort
) {
    public static <T> PageResult<T> from(PageInfo<T> pageInfo, SortParam sort) {
        return new PageResult<>(
                pageInfo.getTotal(),
                pageInfo.getPages(),
                pageInfo.getList(),
                new PageParam(pageInfo.getPageNum(), pageInfo.getPageSize()),
                sort
        );
    }
}
