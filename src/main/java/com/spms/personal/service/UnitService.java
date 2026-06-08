package com.spms.personal.service;

import com.spms.base.PageResult;
import com.spms.personal.entity.UnitEntity;
import com.spms.personal.model.UnitPageRequest;

public interface UnitService {
    PageResult<UnitEntity> getPage(UnitPageRequest request);

    UnitEntity getDetail(Long id);

    UnitEntity add(UnitEntity unit);

    UnitEntity update(UnitEntity unit);

    void delete(Long id);
}
