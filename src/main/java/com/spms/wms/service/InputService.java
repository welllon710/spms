package com.spms.wms.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.wms.entity.InputEntity;
import com.spms.wms.model.InputFinishRequest;
import com.spms.wms.model.InputPageFilter;

import java.util.Map;

public interface InputService {
    PageResult<InputEntity> getPage(PageQuery<InputPageFilter> request);

    void add(InputEntity request);

    void update(InputEntity request);

    InputEntity getDetail(Map<String, Object> request);

    void audit(InputEntity request);

    void reject(InputEntity request);

    void addFinish(InputFinishRequest request);
}
