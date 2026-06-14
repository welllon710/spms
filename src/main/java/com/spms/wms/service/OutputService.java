package com.spms.wms.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.wms.entity.OutputEntity;
import com.spms.wms.model.OutputFinishRequest;
import com.spms.wms.model.OutputPageFilter;

import java.util.Map;

public interface OutputService {
    PageResult<OutputEntity> getPage(PageQuery<OutputPageFilter> request);

    void add(OutputEntity request);

    void update(OutputEntity request);

    OutputEntity getDetail(Map<String, Object> request);

    void audit(OutputEntity request);

    void reject(OutputEntity request);

    void addFinish(OutputFinishRequest request);
}
