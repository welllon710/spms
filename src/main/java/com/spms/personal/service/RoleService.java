package com.spms.personal.service;

import com.spms.base.PageResult;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.model.RolePageRequest;

public interface RoleService {
    PageResult<RoleEntity> getPage(RolePageRequest request);

    RoleEntity getDetail(Long id);

    RoleEntity add(RoleEntity role);

    RoleEntity update(RoleEntity role);

    void delete(Long id);
}
