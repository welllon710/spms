package com.spms.channel.controller;


import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.service.PurchaseService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Api("purchase")
@RequiredArgsConstructor
@Permission
public class PurchaseController extends ApiController {

    private final PurchaseService purchaseService;

    @PostMapping("getPage")

    public Json<PageResult<PurchaseEntity>> getPage(@RequestBody PageQuery<PurchasePageFilter> request) {
        return Json.data(purchaseService.getPage(request), "'请求成功'");
    }



    @PostMapping("add")
    public Json getPurchaseList(@RequestBody PurchasePageFilter request) {
        purchaseService.add(request);
        return Json.success("采购成功");
    }

    @PostMapping("getDetail")
    public Json<PurchaseEntity> getPurchaseDetail(@RequestBody Map<String,Object> request) {
        return Json.data(purchaseService.getDetail(request));
    }

}