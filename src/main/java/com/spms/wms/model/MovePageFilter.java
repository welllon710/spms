package com.spms.wms.model;

public record MovePageFilter(
        String billCode,
        Integer status,
        Long storageId
) {
}
