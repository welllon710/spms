package com.spms.personal.service;

import com.spms.base.PageResult;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.model.PermissionPageRequest;

public interface PermissionService {
    PageResult<PermissionEntity> getPage(PermissionPageRequest request);

    PermissionEntity getDetail(Long id);

    PermissionEntity add(PermissionEntity permission);

    PermissionEntity update(PermissionEntity permission);

    void delete(Long id);
}
