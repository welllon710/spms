package com.spms.wms.model;

import com.spms.wms.entity.StorageEntity;

public record InventoryPageFilter(
        String type,
        Long storageId,
        StorageEntity storage
) {
}
