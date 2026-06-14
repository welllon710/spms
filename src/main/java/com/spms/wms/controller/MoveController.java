package com.spms.wms.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.IdRequest;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import com.spms.base.RejectRequest;
import com.spms.wms.entity.MoveEntity;
import com.spms.wms.model.MoveAddRequest;
import com.spms.wms.model.MoveFinishRequest;
import com.spms.wms.model.MoveUpdateRequest;
import com.spms.wms.model.MovePageFilter;
import com.spms.wms.service.MoveService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("move")
@Permission
@RequiredArgsConstructor
public class MoveController extends ApiController {
    private final MoveService moveService;

    @PostMapping("getPage")
    public Json<PageResult<MoveEntity>> getPage(@RequestBody(required = false) PageQuery<MovePageFilter> request) {
        return Json.data(moveService.getPage(request));
    }

    @PostMapping("getDetail")
    public Json<MoveEntity> getDetail(@RequestBody @Valid IdRequest request) {
        return Json.data(moveService.getDetail(request));
    }

    @PostMapping("add")
    public Json<String> add(@RequestBody @Valid MoveAddRequest request) {
        moveService.add(request);
        return Json.success("新增成功");
    }

    @PostMapping("update")
    public Json<String> update(@RequestBody @Valid MoveUpdateRequest request) {
        moveService.update(request);
        return Json.success("操作成功");
    }

    @PostMapping("audit")
    public Json<String> audit(@RequestBody @Valid IdRequest request) {
        moveService.audit(request);
        return Json.success("审批成功");
    }

    @PostMapping("reject")
    public Json<String> reject(@RequestBody @Valid RejectRequest request) {
        moveService.reject(request);
        return Json.success("驳回成功");
    }

    @PostMapping("addFinish")
    public Json<String> addFinish(@RequestBody @Valid MoveFinishRequest request) {
        moveService.addFinish(request);
        return Json.success("操作成功");
    }
}
