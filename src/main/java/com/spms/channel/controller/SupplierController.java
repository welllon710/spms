package com.spms.channel.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.channel.entity.SupplierEntity;
import com.spms.channel.model.SupplierPageFilter;
import com.spms.channel.service.SupplierService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("supplier")
@Permission
@RequiredArgsConstructor
public class SupplierController extends ApiController {
    private final SupplierService supplierService;

    @PostMapping("/getPage")
    public Json<PageResult<SupplierEntity>> getPage(@RequestBody(required = false) PageQuery<SupplierPageFilter> request) {
        return Json.data(supplierService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<SupplierEntity> getDetail(@RequestBody SupplierEntity supplier) {
        return Json.data(supplierService.getDetail(getSupplierId(supplier)));
    }

    @PostMapping("/add")
    public Json<SupplierEntity> add(@RequestBody SupplierEntity supplier) {
        return Json.data(supplierService.add(supplier), "新增成功");
    }

    @PostMapping("/update")
    public Json<SupplierEntity> update(@RequestBody SupplierEntity supplier) {
        return Json.data(supplierService.update(supplier), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody SupplierEntity supplier) {
        supplierService.delete(getSupplierId(supplier));
        return Json.success("删除成功");
    }

    private Long getSupplierId(SupplierEntity supplier) {
        return supplier == null ? null : supplier.getId();
    }
}
