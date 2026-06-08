package com.spms.personal.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.personal.entity.UnitEntity;
import com.spms.personal.model.UnitPageFilter;

public interface UnitService {
    PageResult<UnitEntity> getPage(PageQuery<UnitPageFilter> request);

    UnitEntity getDetail(Long id);

    UnitEntity add(UnitEntity unit);

    UnitEntity update(UnitEntity unit);

    void delete(Long id);
}
