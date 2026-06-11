package com.spms.asset.controller;

import com.spms.asset.entity.MaterialEntity;
import com.spms.asset.model.MaterialPageFilter;
import com.spms.asset.service.MaterialService;
import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("material")
@Permission
@RequiredArgsConstructor
public class MaterialController extends ApiController {
    private final MaterialService materialService;

    @PostMapping("/getPage")
    public Json<PageResult<MaterialEntity>> getPage(@RequestBody(required = false) PageQuery<MaterialPageFilter> request) {
        return Json.data(materialService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<MaterialEntity> getDetail(@RequestBody MaterialEntity material) {
        return Json.data(materialService.getDetail(getMaterialId(material)));
    }

    @PostMapping("/add")
    public Json<MaterialEntity> add(@RequestBody MaterialEntity material) {
        return Json.data(materialService.add(material), "新增成功");
    }

    @PostMapping("/update")
    public Json<MaterialEntity> update(@RequestBody MaterialEntity material) {
        return Json.data(materialService.update(material), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody MaterialEntity material) {
        materialService.delete(getMaterialId(material));
        return Json.success("删除成功");
    }

    private Long getMaterialId(MaterialEntity material) {
        return material == null ? null : material.getId();
    }
}
