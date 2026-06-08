package com.spms.personal.service;

import com.spms.base.PageResult;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.model.MenuPageRequest;

public interface MenuService {
    PageResult<MenuEntity> getPage(MenuPageRequest request);

    MenuEntity getDetail(Long id);

    MenuEntity add(MenuEntity menu);

    MenuEntity update(MenuEntity menu);

    void delete(Long id);
}
