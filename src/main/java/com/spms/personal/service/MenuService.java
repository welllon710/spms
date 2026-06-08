package com.spms.personal.service;

import com.spms.base.PageQuery;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.model.MenuPageFilter;

import java.util.List;

public interface MenuService {
    List<MenuEntity> getPage(PageQuery<MenuPageFilter> request);

    MenuEntity getDetail(Long id);

    MenuEntity add(MenuEntity menu);

    MenuEntity update(MenuEntity menu);

    void delete(Long id);
}
