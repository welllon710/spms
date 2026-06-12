package com.spms.asset.controller;

import com.spms.asset.entity.DeviceEntity;
import com.spms.asset.model.DevicePageFilter;
import com.spms.asset.service.DeviceService;
import com.spms.base.Api;
import com.spms.base.ApiController;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api("device")
@Permission
@RequiredArgsConstructor
public class DeviceController extends ApiController {
    private final DeviceService deviceService;

    @PostMapping("/getPage")
    public Json<PageResult<DeviceEntity>> getPage(@RequestBody(required = false) PageQuery<DevicePageFilter> request) {
        return Json.data(deviceService.getPage(request));
    }

    @PostMapping("/getDetail")
    public Json<DeviceEntity> getDetail(@RequestBody DeviceEntity device) {
        return Json.data(deviceService.getDetail(getDeviceId(device)));
    }

    @PostMapping("/add")
    public Json<DeviceEntity> add(@RequestBody DeviceEntity device) {
        return Json.data(deviceService.add(device), "新增成功");
    }

    @PostMapping("/update")
    public Json<DeviceEntity> update(@RequestBody DeviceEntity device) {
        return Json.data(deviceService.update(device), "修改成功");
    }

    @PostMapping("/delete")
    public Json<String> delete(@RequestBody DeviceEntity device) {
        deviceService.delete(getDeviceId(device));
        return Json.success("删除成功");
    }

    private Long getDeviceId(DeviceEntity device) {
        return device == null ? null : device.getId();
    }
}
