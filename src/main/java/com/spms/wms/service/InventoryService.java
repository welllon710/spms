package com.spms.wms.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.wms.entity.InventoryEntity;
import com.spms.wms.model.InventoryPageFilter;
import jakarta.validation.Valid;

import java.util.List;

public interface InventoryService {
    PageResult<InventoryEntity> getPage(PageQuery<InventoryPageFilter> request);

    InventoryEntity getDetail(Long id);

    List<InventoryEntity> getList(@Valid PageQuery<InventoryPageFilter> request);
}
