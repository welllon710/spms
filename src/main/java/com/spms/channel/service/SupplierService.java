package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.SupplierEntity;
import com.spms.channel.model.SupplierPageFilter;
import com.spms.common.result.PageResult;

public interface SupplierService {
    PageResult<SupplierEntity> getPage(PageQuery<SupplierPageFilter> request);

    SupplierEntity getDetail(Long id);

    SupplierEntity add(SupplierEntity supplier);

    SupplierEntity update(SupplierEntity supplier);

    void delete(Long id);
}
