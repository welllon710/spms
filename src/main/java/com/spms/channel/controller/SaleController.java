package com.spms.channel.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.channel.entity.SaleEntity;
import com.spms.channel.model.SalePageFilter;
import com.spms.channel.service.SaleService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Api("sale")
@Permission
@RequiredArgsConstructor
public class SaleController extends ApiController {
    private final SaleService saleService;

    @PostMapping("getPage")
    public Json<PageResult<SaleEntity>> getPage(@RequestBody(required = false) PageQuery<SalePageFilter> request) {
        return Json.data(saleService.getPage(request));
    }

    @PostMapping("add")
    public Json<String> add(@RequestBody SalePageFilter request) {
        saleService.add(request);
        return Json.success("销售成功");
    }

    @PostMapping("update")
    public Json<String> update(@RequestBody SaleEntity request) {
        saleService.update(request);
        return Json.success("操作成功");
    }

    @PostMapping("getDetail")
    public Json<SaleEntity> getDetail(@RequestBody Map<String, Object> request) {
        return Json.data(saleService.getDetail(request));
    }

    @PostMapping("audit")
    public Json<String> audit(@RequestBody SaleEntity request) {
        saleService.audit(request);
        return Json.success("审批成功");
    }

    @PostMapping("reject")
    public Json<String> reject(@RequestBody SaleEntity request) {
        saleService.reject(request);
        return Json.success("驳回成功");
    }
}
