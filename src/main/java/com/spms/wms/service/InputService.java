package com.spms.wms.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.wms.entity.InputEntity;
import com.spms.wms.model.InputAddRequest;
import com.spms.wms.model.InputFinishRequest;
import com.spms.wms.model.InputUpdateRequest;
import com.spms.base.IdRequest;
import com.spms.base.RejectRequest;
import com.spms.wms.model.InputPageFilter;

public interface InputService {
    PageResult<InputEntity> getPage(PageQuery<InputPageFilter> request);

    void add(InputAddRequest request);

    void update(InputUpdateRequest request);

    InputEntity getDetail(IdRequest request);

    void audit(IdRequest request);

    void reject(RejectRequest request);

    void addFinish(InputFinishRequest request);
}
