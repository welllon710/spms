package com.spms.wms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.wms.entity.InventoryEntity;
import com.spms.wms.mapper.InventoryMapper;
import com.spms.wms.model.InventoryPageFilter;
import com.spms.wms.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;


@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends BaseService<InventoryEntity> implements InventoryService {

    private final InventoryMapper inventoryMapper;

    @Override
    public PageResult<InventoryEntity> getPage(PageQuery<InventoryPageFilter> request) {
        Map<String, Object> params = buildParams(request == null ? null : request.filter());
        Page<InventoryEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(inventoryMapper.getPageList(page, params), null);
    }

    @Override
    public InventoryEntity getDetail(Long id) {
        requireId(id, "库存ID不能为空");
        InventoryEntity inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "库存不存在");
        }
        return inventory;
    }

    @Override
    public List<InventoryEntity> getList(PageQuery<InventoryPageFilter> request) {
        return inventoryMapper.getList(buildParams(request == null ? null : request.filter()));
    }

    private Map<String, Object> buildParams(InventoryPageFilter filter) {
        return QueryParams.of(filter)
                .putTrim("type", InventoryPageFilter::type)
                .put("storageId", this::getStorageId)
                .toMap();
    }

    private Long getStorageId(InventoryPageFilter filter) {
        if (filter == null) return null;
        if (filter.storageId() != null) {
            return filter.storageId();
        }
        if (filter.storage() != null) {
            return filter.storage().getId();
        }
        return null;
    }
}
