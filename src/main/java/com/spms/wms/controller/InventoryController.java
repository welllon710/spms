package com.spms.wms.controller;


import com.spms.base.Api;
import com.spms.base.PageQuery;
import com.spms.common.result.Json;
import com.spms.common.result.PageResult;
import com.spms.common.security.Permission;
import com.spms.wms.entity.InventoryEntity;
import com.spms.wms.model.InventoryPageFilter;
import com.spms.wms.service.InventoryService;
import com.spms.base.IdRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Permission
@Api("inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("getPage")
    public Json<PageResult<InventoryEntity>> getPage(@RequestBody PageQuery<InventoryPageFilter> request) {
        return Json.data(inventoryService.getPage(request));
    }

    @PostMapping("getList")
    public Json<List<InventoryEntity>> getList(@RequestBody(required = false) @Valid PageQuery<InventoryPageFilter> request) {
        return Json.data(inventoryService.getList(request));
    }

    @PostMapping("getDetail")
    public Json<InventoryEntity> getDetail(@RequestBody @Valid IdRequest request) {
        return Json.data(inventoryService.getDetail(request.id()));
    }
}
