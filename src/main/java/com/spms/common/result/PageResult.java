package com.spms.common.result;

import com.github.pagehelper.PageInfo;
import com.spms.base.SortParam;

import java.util.List;

public record PageResult<T>(
        long total,
        int pageCount,
        List<T> list,
        Integer pageNum,
        Integer pageSize,
        SortParam sort
) {
    public static <T> PageResult<T> from(PageInfo<T> pageInfo, SortParam sort) {
        return new PageResult<>(
                pageInfo.getTotal(),
                pageInfo.getPages(),
                pageInfo.getList(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                sort
        );
    }
}
