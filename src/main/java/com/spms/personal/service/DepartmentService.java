package com.spms.personal.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.personal.entity.DepartmentEntity;
import com.spms.personal.model.DepartmentPageFilter;

public interface DepartmentService  {
    PageResult<DepartmentEntity> getPage(PageQuery<DepartmentPageFilter> request);

    DepartmentEntity getDetail(Long id);

    DepartmentEntity add(DepartmentEntity department);

    DepartmentEntity update(DepartmentEntity department);

    void delete(Long id);
}
