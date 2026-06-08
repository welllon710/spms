package com.spms.personal.service;

import com.spms.base.PageResult;
import com.spms.personal.entity.DepartmentEntity;
import com.spms.personal.model.DepartmentPageRequest;

public interface DepartmentService {
    PageResult<DepartmentEntity> getPage(DepartmentPageRequest request);

    DepartmentEntity getDetail(Long id);

    DepartmentEntity add(DepartmentEntity department);

    DepartmentEntity update(DepartmentEntity department);

    void delete(Long id);
}
