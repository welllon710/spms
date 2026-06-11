package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;

public interface PurchaseService {

    PageResult<PurchaseEntity> getPage(PageQuery<PurchasePageFilter> request);

    void add(PurchasePageFilter request);
}
