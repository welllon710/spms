package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.base.IdRequest;
import com.spms.base.RejectRequest;
import com.spms.channel.model.PurchaseAddRequest;
import com.spms.channel.model.PurchaseFinishRequest;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.model.PurchaseUpdateRequest;
import com.spms.common.result.PageResult;

public interface PurchaseService {

    PageResult<PurchaseEntity> getPage(PageQuery<PurchasePageFilter> request);

    void add(PurchaseAddRequest request);

    PurchaseEntity getDetail(IdRequest request);

    void audit(IdRequest request);

    void reject(RejectRequest request);

    void update(PurchaseUpdateRequest request);

    void addFinish(PurchaseFinishRequest request);
}
