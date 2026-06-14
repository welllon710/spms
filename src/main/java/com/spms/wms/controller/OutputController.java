package com.spms.wms.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.IdRequest;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import com.spms.base.RejectRequest;
import com.spms.wms.entity.OutputEntity;
import com.spms.wms.model.OutputAddRequest;
import com.spms.wms.model.OutputFinishRequest;
import com.spms.wms.model.OutputUpdateRequest;
import com.spms.wms.model.OutputPageFilter;
import com.spms.wms.service.OutputService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("output")
@Permission
@RequiredArgsConstructor
public class OutputController extends ApiController {
    private final OutputService outputService;

    @PostMapping("getPage")
    public Json<PageResult<OutputEntity>> getPage(@RequestBody(required = false) PageQuery<OutputPageFilter> request) {
        return Json.data(outputService.getPage(request));
    }

    @PostMapping("getDetail")
    public Json<OutputEntity> getDetail(@RequestBody @Valid IdRequest request) {
        return Json.data(outputService.getDetail(request));
    }

    @PostMapping("add")
    public Json<String> add(@RequestBody @Valid OutputAddRequest request) {
        outputService.add(request);
        return Json.success("新增成功");
    }

    @PostMapping("update")
    public Json<String> update(@RequestBody @Valid OutputUpdateRequest request) {
        outputService.update(request);
        return Json.success("操作成功");
    }

    @PostMapping("audit")
    public Json<String> audit(@RequestBody @Valid IdRequest request) {
        outputService.audit(request);
        return Json.success("审批成功");
    }

    @PostMapping("reject")
    public Json<String> reject(@RequestBody @Valid RejectRequest request) {
        outputService.reject(request);
        return Json.success("驳回成功");
    }

    @PostMapping("addFinish")
    public Json<String> addFinish(@RequestBody @Valid OutputFinishRequest request) {
        outputService.addFinish(request);
        return Json.success("操作成功");
    }
}
