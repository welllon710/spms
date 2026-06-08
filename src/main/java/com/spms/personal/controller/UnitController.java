package com.spms.personal.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageResult;
import com.spms.common.result.Json;
import com.spms.common.security.Permission;
import com.spms.personal.entity.UnitEntity;
import com.spms.personal.model.UnitPageRequest;
import com.spms.personal.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("unit")
@Permission
@RequiredArgsConstructor
public class UnitController extends ApiController {
    private final UnitService unitService;

    @PostMapping("/getPage")
    public Json<PageResult<UnitEntity>> getPage(@RequestBody(required = false) UnitPageRequest request) {
        return Json.data(unitService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<UnitEntity> getDetail(@RequestBody UnitEntity unit) {
        return Json.data(unitService.getDetail(getUnitId(unit)));
    }

    @PostMapping("/add")
    public Json<UnitEntity> add(@RequestBody UnitEntity unit) {
        return Json.data(unitService.add(unit), "新增成功");
    }

    @PostMapping("/update")
    public Json<UnitEntity> update(@RequestBody UnitEntity unit) {
        return Json.data(unitService.update(unit), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody UnitEntity unit) {
        unitService.delete(getUnitId(unit));
        return Json.success("删除成功");
    }

    private Long getUnitId(UnitEntity unit) {
        return unit == null ? null : unit.getId();
    }
}
