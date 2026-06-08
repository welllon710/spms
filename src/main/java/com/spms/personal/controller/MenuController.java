package com.spms.personal.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.security.Permission;
import com.spms.personal.entity.MenuEntity;
import com.spms.personal.model.MenuPageFilter;
import com.spms.personal.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api("menu")
@Permission
@RequiredArgsConstructor
public class MenuController extends ApiController {
    private final MenuService menuService;

    @PostMapping("/getList")
    public Json<List<MenuEntity>> getPage(@RequestBody(required = false) PageQuery<MenuPageFilter> request) {
        return Json.data(menuService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<MenuEntity> getDetail(@RequestBody MenuEntity menu) {
        return Json.data(menuService.getDetail(getMenuId(menu)));
    }

    @PostMapping("/add")
    public Json<MenuEntity> add(@RequestBody MenuEntity menu) {
        return Json.data(menuService.add(menu), "新增成功");
    }

    @PostMapping("/update")
    public Json<MenuEntity> update(@RequestBody MenuEntity menu) {
        return Json.data(menuService.update(menu), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody MenuEntity menu) {
        menuService.delete(getMenuId(menu));
        return Json.success("删除成功");
    }

    private Long getMenuId(MenuEntity menu) {
        return menu == null ? null : menu.getId();
    }
}
