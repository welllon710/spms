package com.spms.base;

public record PageQuery<T>(
        T filter,
        Integer pageNum,
        Integer pageSize
) {
}
