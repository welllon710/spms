package com.spms.channel.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.channel.entity.SalePriceEntity;
import com.spms.channel.entity.SalePriceEntity;
import com.spms.channel.model.PurchasePricePageFilter;
import com.spms.channel.service.PurchasePriceService;
import com.spms.channel.service.SalePriceService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Permission
@Api("salePrice")
@AllArgsConstructor
public class SalePriceController extends ApiController {

    private final SalePriceService salePriceService;
    @PostMapping("getPage")
    public Json<PageResult<SalePriceEntity>> getPage(@RequestBody(required = false) PageQuery<PurchasePricePageFilter> request) {
        return Json.data(salePriceService.getPage(request));
    }

    @PostMapping("add")
    public Json add(@RequestBody SalePriceEntity SalePriceEntity) {
        salePriceService.add(SalePriceEntity);
        return Json.success("添加成功");
    }

    @PostMapping("getDetail")
    public Json<SalePriceEntity> getDetail(@RequestBody Map<String, String> map) {
        return Json.data(salePriceService.getById(map.get("id")));
    }

    @PostMapping("update")
    public Json update(@RequestBody SalePriceEntity SalePriceEntity) {
        salePriceService.update(SalePriceEntity);
        return Json.success("更新成功");
    }
}
