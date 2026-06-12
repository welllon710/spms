package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;

import java.util.Map;

public interface PurchaseService {

    PageResult<PurchaseEntity> getPage(PageQuery<PurchasePageFilter> request);

    void add(PurchasePageFilter request);

    PurchaseEntity getDetail(Map<String, Object> request);

    void audit(PurchaseEntity request);

    void reject(PurchaseEntity request);

    void update(PurchaseEntity request);

    void addFinish(Map<String, Long> request);
}
