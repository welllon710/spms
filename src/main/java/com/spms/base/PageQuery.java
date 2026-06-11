package com.spms.base;

public record PageQuery<T>(
        T filter,
        Object page,
        Integer pageNum,
        Integer pageSize
) {
}
