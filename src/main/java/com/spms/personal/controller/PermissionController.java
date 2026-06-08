package com.spms.personal.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.common.result.Json;
import com.spms.common.security.Permission;
import com.spms.personal.entity.PermissionEntity;
import com.spms.personal.model.PermissionPageFilter;
import com.spms.personal.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("permission")
@Permission
@RequiredArgsConstructor
public class PermissionController extends ApiController {
    private final PermissionService permissionService;

    @PostMapping("/getPage")
    public Json<PageResult<PermissionEntity>> getPage(@RequestBody(required = false) PageQuery<PermissionPageFilter> request) {
        return Json.data(permissionService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<PermissionEntity> getDetail(@RequestBody PermissionEntity permission) {
        return Json.data(permissionService.getDetail(getPermissionId(permission)));
    }

    @PostMapping("/add")
    public Json<PermissionEntity> add(@RequestBody PermissionEntity permission) {
        return Json.data(permissionService.add(permission), "新增成功");
    }

    @PostMapping("/update")
    public Json<PermissionEntity> update(@RequestBody PermissionEntity permission) {
        return Json.data(permissionService.update(permission), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody PermissionEntity permission) {
        permissionService.delete(getPermissionId(permission));
        return Json.success("删除成功");
    }

    private Long getPermissionId(PermissionEntity permission) {
        return permission == null ? null : permission.getId();
    }
}
