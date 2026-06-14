package com.spms.channel.controller;


import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.IdRequest;
import com.spms.base.PageQuery;
import com.spms.base.RejectRequest;
import com.spms.channel.entity.PurchaseEntity;
import com.spms.channel.model.PurchaseAddRequest;
import com.spms.channel.model.PurchaseFinishRequest;
import com.spms.channel.model.PurchaseUpdateRequest;
import com.spms.channel.model.PurchasePageFilter;
import com.spms.channel.service.PurchaseService;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("purchase")
@RequiredArgsConstructor
@Permission
public class PurchaseController extends ApiController {

    private final PurchaseService purchaseService;

    @PostMapping("getPage")
    public Json<PageResult<PurchaseEntity>> getPage(@RequestBody PageQuery<PurchasePageFilter> request) {
        return Json.data(purchaseService.getPage(request));
    }

    @PostMapping("add")
    public Json<String> add(@RequestBody @Valid PurchaseAddRequest request) {
        purchaseService.add(request);
        return Json.success("采购成功");
    }

    @PostMapping("update")
    public Json<String> update(@RequestBody @Valid PurchaseUpdateRequest request) {
        purchaseService.update(request);
        return Json.success("操作成功");
    }

    @PostMapping("getDetail")
    public Json<PurchaseEntity> getPurchaseDetail(@RequestBody @Valid IdRequest request) {
        return Json.data(purchaseService.getDetail(request));
    }

    @PostMapping("audit")
    public Json<String> audit(@RequestBody @Valid IdRequest request) {
        purchaseService.audit(request);
        return Json.success("审批成功");
    }

    @PostMapping("reject")
    public Json<String> reject(@RequestBody @Valid RejectRequest request) {
        purchaseService.reject(request);
        return Json.success("驳回成功");
    }

    @PostMapping("addFinish")
    public Json<String> addFinish(@RequestBody @Valid PurchaseFinishRequest request) {
        purchaseService.addFinish(request);
        return Json.success("操作成功");
    }

}