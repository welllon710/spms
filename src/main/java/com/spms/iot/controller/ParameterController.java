package com.spms.iot.controller;

import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import com.spms.iot.entity.ParameterEntity;
import com.spms.iot.model.ParameterPageFilter;
import com.spms.iot.service.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("parameter")
@Permission
@RequiredArgsConstructor
public class ParameterController extends ApiController {
    private final ParameterService parameterService;

    @PostMapping("/getPage")
    public Json<PageResult<ParameterEntity>> getPage(@RequestBody(required = false) PageQuery<ParameterPageFilter> request) {
        return Json.data(parameterService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<ParameterEntity> getDetail(@RequestBody ParameterEntity parameter) {
        return Json.data(parameterService.getDetail(getParameterId(parameter)));
    }

    @PostMapping("/add")
    public Json<ParameterEntity> add(@RequestBody ParameterEntity parameter) {
        return Json.data(parameterService.add(parameter), "新增成功");
    }

    @PostMapping("/update")
    public Json<ParameterEntity> update(@RequestBody ParameterEntity parameter) {
        return Json.data(parameterService.update(parameter), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody ParameterEntity parameter) {
        parameterService.delete(getParameterId(parameter));
        return Json.success("删除成功");
    }

    private Long getParameterId(ParameterEntity parameter) {
        return parameter == null ? null : parameter.getId();
    }
}
