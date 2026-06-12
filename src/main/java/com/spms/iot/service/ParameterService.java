package com.spms.iot.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.iot.entity.ParameterEntity;
import com.spms.iot.model.ParameterPageFilter;

public interface ParameterService {
    PageResult<ParameterEntity> getPage(PageQuery<ParameterPageFilter> request);

    ParameterEntity getDetail(Long id);

    ParameterEntity add(ParameterEntity parameter);

    ParameterEntity update(ParameterEntity parameter);

    void delete(Long id);
}
