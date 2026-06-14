package com.spms.wms.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.IdRequest;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import com.spms.base.RejectRequest;
import com.spms.wms.entity.InputEntity;
import com.spms.wms.model.InputAddRequest;
import com.spms.wms.model.InputFinishRequest;
import com.spms.wms.model.InputUpdateRequest;
import com.spms.wms.model.InputPageFilter;
import com.spms.wms.service.InputService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("input")
@Permission
@RequiredArgsConstructor
public class InputController extends ApiController {
    private final InputService inputService;

    @PostMapping("getPage")
    public Json<PageResult<InputEntity>> getPage(@RequestBody(required = false) PageQuery<InputPageFilter> request) {
        return Json.data(inputService.getPage(request));
    }

    @PostMapping("getDetail")
    public Json<InputEntity> getDetail(@RequestBody @Valid IdRequest request) {
        return Json.data(inputService.getDetail(request));
    }

    @PostMapping("add")
    public Json<String> add(@RequestBody @Valid InputAddRequest request) {
        inputService.add(request);
        return Json.success("新增成功");
    }

    @PostMapping("update")
    public Json<String> update(@RequestBody @Valid InputUpdateRequest request) {
        inputService.update(request);
        return Json.success("操作成功");
    }

    @PostMapping("audit")
    public Json<String> audit(@RequestBody @Valid IdRequest request) {
        inputService.audit(request);
        return Json.success("审批成功");
    }

    @PostMapping("reject")
    public Json<String> reject(@RequestBody @Valid RejectRequest request) {
        inputService.reject(request);
        return Json.success("驳回成功");
    }

    @PostMapping("addFinish")
    public Json<String> addFinish(@RequestBody @Valid InputFinishRequest request) {
        inputService.addFinish(request);
        return Json.success("操作成功");
    }
}
