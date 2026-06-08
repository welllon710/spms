package com.spms.personal.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.model.PermissionPageFilter;

public interface PermissionService {
    PageResult<PermissionEntity> getPage(PageQuery<PermissionPageFilter> request);

    PermissionEntity getDetail(Long id);

    PermissionEntity add(PermissionEntity permission);

    PermissionEntity update(PermissionEntity permission);

    void delete(Long id);
}
