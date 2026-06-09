package com.spms.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
    public static <T> PageResult<T> from(IPage<T> page, SortParam sort) {
        return new PageResult<>(
                page.getTotal(),
                (int) page.getPages(),
                page.getRecords(),
                (int) page.getCurrent(),
                (int) page.getSize(),
                sort
        );
    }
}
