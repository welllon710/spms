package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchasePriceEntity;
import com.spms.channel.model.PurchasePricePageFilter;
import com.spms.common.result.PageResult;

public interface PurchasePriceService {
    void add(PurchasePriceEntity purchasePriceEntity);

    PageResult<PurchasePriceEntity> getPage(PageQuery<PurchasePricePageFilter> request);

    PurchasePriceEntity getById(String id);

    void update(PurchasePriceEntity purchasePriceEntity);

    PurchasePriceEntity getByMaterialAndSupplier(PurchasePriceEntity request);
}
