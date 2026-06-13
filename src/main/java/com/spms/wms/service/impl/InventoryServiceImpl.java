package com.spms.wms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.wms.entity.InventoryEntity;
import com.spms.wms.mapper.InventoryMapper;
import com.spms.wms.model.InventoryPageFilter;
import com.spms.wms.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.spms.common.util.ParamUtils.requireNotNull;


@Service
@AllArgsConstructor
public class InventoryServiceImpl extends BaseService<InventoryEntity> implements InventoryService {

    private final InventoryMapper inventoryMapper;

    @Override
    public PageResult<InventoryEntity> getPage(PageQuery<InventoryPageFilter> request) {
        requireNotNull(request, "请求参数不能为空");

        InventoryPageFilter filter = request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("type", InventoryPageFilter::type)
                .put("storageId", this::getStorageId)
                .toMap();

        Page<InventoryEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(inventoryMapper.getPageList(page, params), null);
    }

    private Long getStorageId(InventoryPageFilter filter) {
        if (filter.storageId() != null) {
            return filter.storageId();
        }
        if (filter.storage() != null) {
            return filter.storage().getId();
        }
        return null;
    }
}
