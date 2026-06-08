package com.spms.personal.service;

import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.personal.dto.AuthorizeMenuDto;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.model.RolePageFilter;

public interface RoleService {
    PageResult<RoleEntity> getPage(PageQuery<RolePageFilter> request);

    RoleEntity getDetail(Long id);

    RoleEntity add(RoleEntity role);

    RoleEntity update(RoleEntity role);

    void delete(Long id);

    void authorizeMenu(AuthorizeMenuDto dto);
}
