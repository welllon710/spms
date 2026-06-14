package com.spms.wms.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.wms.entity.MoveEntity;
import com.spms.wms.model.MoveAddRequest;
import com.spms.wms.model.MoveFinishRequest;
import com.spms.wms.model.MoveUpdateRequest;
import com.spms.base.IdRequest;
import com.spms.base.RejectRequest;
import com.spms.wms.model.MovePageFilter;

public interface MoveService {
    PageResult<MoveEntity> getPage(PageQuery<MovePageFilter> request);

    MoveEntity getDetail(IdRequest request);

    void add(MoveAddRequest request);

    void update(MoveUpdateRequest request);

    void audit(IdRequest request);

    void reject(RejectRequest request);

    void addFinish(MoveFinishRequest request);
}
