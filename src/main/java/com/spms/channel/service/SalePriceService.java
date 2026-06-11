package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.SalePriceEntity;
import com.spms.channel.model.PurchasePricePageFilter;
import com.spms.common.result.PageResult;

public interface SalePriceService {
    PageResult<SalePriceEntity> getPage(PageQuery<PurchasePricePageFilter> request);

    void add(SalePriceEntity salePriceEntity);

    SalePriceEntity getById(String id);

    void update(SalePriceEntity salePriceEntity);
}
