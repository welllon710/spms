package com.spms.base;

public record PageQuery<T>(
        T filter,
        PageParams page,
        Integer pageNum,
        Integer pageSize
) {
    public PageQuery(T filter, Integer pageNum, Integer pageSize) {
        this(filter, null, pageNum, pageSize);
    }
}
