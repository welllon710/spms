package com.spms.wms.model;

import com.spms.wms.entity.StorageEntity;

public record InputFinishRequest(
        Long id,
        Double quantity,
        Long storageId,
        StorageEntity storage
) {
}
