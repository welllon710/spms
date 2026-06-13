package com.spms.wms.controller;


import com.spms.base.Api;
import com.spms.common.result.Json;
import com.spms.common.security.Permission;
import com.spms.wms.entity.StorageEntity;
import com.spms.wms.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Api("storage")
@Permission
@AllArgsConstructor
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
    public Json<StorageEntity> getDetail(@RequestBody Map<String, String> map) {
        return Json.data(storageService.getById(map));
    }

}
