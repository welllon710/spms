package com.spms.wms.service;

import com.spms.wms.entity.StorageEntity;

import com.spms.base.IdRequest;
import java.util.List;

public interface StorageService {
    List<StorageEntity> getList();

    void add(StorageEntity storageEntity);

    void updateById(StorageEntity storageEntity);

    StorageEntity getById(IdRequest request);

    void delete(IdRequest request);
}
