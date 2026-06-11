package com.spms.channel.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.channel.entity.PurchasePriceEntity;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.model.PurchasePricePageFilter;
import com.spms.channel.service.PurchasePriceService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Permission
@Api("purchasePrice")
@RequiredArgsConstructor
public class PurchasePriceController  extends ApiController {

    private final PurchasePriceService purchasePriceService;

    @PostMapping("getPage")
    public Json<PageResult<PurchasePriceEntity>> getPage(@RequestBody(required = false) PageQuery<PurchasePricePageFilter> request) {
        return Json.data(purchasePriceService.getPage(request));
    }

    @PostMapping("add")
    public Json add(@RequestBody PurchasePriceEntity purchasePriceEntity) {
        purchasePriceService.add(purchasePriceEntity);
        return Json.success("添加成功");
    }

    @PostMapping("getDetail")
    public Json<PurchasePriceEntity> getDetail(@RequestBody Map<String, String> map) {
        return Json.data(purchasePriceService.getById(map.get("id")));
    }

    @PostMapping("update")
    public Json update(@RequestBody PurchasePriceEntity purchasePriceEntity) {
        purchasePriceService.update(purchasePriceEntity);
        return Json.success("更新成功");
    }

    @PostMapping("getByMaterialAndSupplier")
    public Json<PurchasePriceEntity> getByMaterialAndSupplier(@RequestBody PurchasePriceEntity request) {

        return Json.data(purchasePriceService.getByMaterialAndSupplier(request));
    };
}
