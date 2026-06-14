package com.spms.channel.service;

import com.spms.base.PageQuery;
import com.spms.channel.entity.SaleEntity;
import com.spms.base.IdRequest;
import com.spms.base.RejectRequest;
import com.spms.channel.model.SaleAddRequest;
import com.spms.channel.model.SalePageFilter;
import com.spms.channel.model.SaleUpdateRequest;
import com.spms.common.result.PageResult;

public interface SaleService {
    PageResult<SaleEntity> getPage(PageQuery<SalePageFilter> request);

    void add(SaleAddRequest request);

    void update(SaleUpdateRequest request);

    SaleEntity getDetail(IdRequest request);

    void audit(IdRequest request);

    void reject(RejectRequest request);
}
