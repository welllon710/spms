package com.spms.wms.service;

import com.spms.wms.entity.StorageEntity;

import java.util.List;
import java.util.Map;

public interface StorageService {
    List<StorageEntity> getList();

    void add(StorageEntity storageEntity);

    void updateById(StorageEntity storageEntity);

    StorageEntity getById(Map<String, String> map);
}
