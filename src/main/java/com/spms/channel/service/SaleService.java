package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.SaleEntity;
import com.spms.channel.model.SalePageFilter;
import com.spms.common.result.PageResult;

import java.util.Map;

public interface SaleService {
    PageResult<SaleEntity> getPage(PageQuery<SalePageFilter> request);

    void add(SalePageFilter request);

    void update(SaleEntity request);

    SaleEntity getDetail(Map<String, Object> request);

    void audit(SaleEntity request);

    void reject(SaleEntity request);
}
