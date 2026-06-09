package com.spms.base;

import lombok.Data;


public record PageQuery<T>(
        T filter,
        PageParams page
) {
}
