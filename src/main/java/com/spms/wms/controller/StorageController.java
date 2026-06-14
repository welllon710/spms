package com.spms.wms.controller;


import com.spms.base.Api;
import com.spms.base.IdRequest;
import com.spms.common.result.Json;
import com.spms.common.security.Permission;
import com.spms.wms.entity.StorageEntity;
import com.spms.wms.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Api("storage")
@Permission
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("add")
    public Json<Void> add(@RequestBody StorageEntity storageEntity) {
        storageService.add(storageEntity);
      return Json.success("添加成功");
    };

    @PostMapping("update")
    public Json<Void> update(@RequestBody StorageEntity storageEntity) {
        storageService.updateById(storageEntity);
        return Json.success("更新成功");
    }

    @PostMapping("getList")
    public Json<List<StorageEntity>> getList() {
        return Json.data(storageService.getList());
    }

    @PostMapping("getDetail")
    public Json<StorageEntity> getDetail(@RequestBody @Valid IdRequest request) {
        return Json.data(storageService.getById(request));
    }

    @PostMapping("delete")
    public Json<String> delete(@RequestBody @Valid IdRequest request) {
        storageService.delete(request);
        return Json.success("删除成功");
    }

}
