package com.spms.personal.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.common.result.Json;
import com.spms.common.security.Permission;
import com.spms.personal.entity.DepartmentEntity;
import com.spms.personal.model.DepartmentPageFilter;
import com.spms.personal.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api("department")
@Permission
@RequiredArgsConstructor
public class DepartmentController extends ApiController {
    private final DepartmentService departmentService;

    @PostMapping("/getList")
    public Json<List<DepartmentEntity>> getPage(@RequestBody(required = false) PageQuery<DepartmentPageFilter> request) {
        return Json.data(departmentService.getPage(request));
    }



    @PostMapping("/getDetail")
    public Json<DepartmentEntity> getDetail(@RequestBody DepartmentEntity department) {
        return Json.data(departmentService.getDetail(getDepartmentId(department)));
    }

    @PostMapping("/add")
    public Json<DepartmentEntity> add(@RequestBody DepartmentEntity department) {
        return Json.data(departmentService.add(department), "新增成功");
    }

    @PostMapping("/update")
    public Json<DepartmentEntity> update(@RequestBody DepartmentEntity department) {
        return Json.data(departmentService.update(department), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody DepartmentEntity department) {
        departmentService.delete(getDepartmentId(department));
        return Json.success("删除成功");
    }

    private Long getDepartmentId(DepartmentEntity department) {
        return department == null ? null : department.getId();
    }
}
