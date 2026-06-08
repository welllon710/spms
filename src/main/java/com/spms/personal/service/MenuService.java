package com.spms.personal.service;

import com.spms.base.PageResult;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.model.MenuPageRequest;

import java.util.List;

public interface MenuService {
    List<MenuEntity> getPage(MenuPageRequest request);

    MenuEntity getDetail(Long id);

    MenuEntity add(MenuEntity menu);

    MenuEntity update(MenuEntity menu);

    void delete(Long id);
}
