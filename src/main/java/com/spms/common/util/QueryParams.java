package com.spms.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class QueryParams<T> {
    private final T filter;
    private final Map<String, Object> params = new HashMap<>();

    private QueryParams(T filter) {
        this.filter = filter;
    }

    public static <T> QueryParams<T> of(T filter) {
        return new QueryParams<>(filter);
    }

    public QueryParams<T> put(String key, Function<T, ?> getter) {
        params.put(key, filter == null ? null : getter.apply(filter));
        return this;
    }

    public QueryParams<T> putTrim(String key, Function<T, String> getter) {
        String value = filter == null ? null : getter.apply(filter);
        params.put(key, ParamUtils.trimToNull(value));
        return this;
    }

    public Map<String, Object> toMap() {
        return params;
    }
}
