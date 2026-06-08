package com.spms.personal.controller;


import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageResult;
import com.spms.common.result.Json;
import com.spms.common.security.Permission;
import com.spms.personal.entity.RoleEntity;
import com.spms.personal.model.RolePageRequest;
import com.spms.personal.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("role")
@Permission
@RequiredArgsConstructor
public class RoleController extends ApiController {
    private final RoleService roleService;

    @PostMapping("/getPage")
    public Json<PageResult<RoleEntity>> getPage(@RequestBody(required = false) RolePageRequest request) {
        return Json.data(roleService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<RoleEntity> getDetail(@RequestBody RoleEntity role) {
        return Json.data(roleService.getDetail(getRoleId(role)));
    }

    @PostMapping("/add")
    public Json<RoleEntity> add(@RequestBody RoleEntity role) {
        return Json.data(roleService.add(role), "新增成功");
    }

    @PostMapping("/update")
    public Json<RoleEntity> update(@RequestBody RoleEntity role) {
        return Json.data(roleService.update(role), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody RoleEntity role) {
        roleService.delete(getRoleId(role));
        return Json.success("删除成功");
    }

    private Long getRoleId(RoleEntity role) {
        return role == null ? null : role.getId();
    }
}
