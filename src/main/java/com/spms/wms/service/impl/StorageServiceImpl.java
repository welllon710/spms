package com.spms.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.spms.base.BaseService;
import com.spms.base.IdRequest;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.util.TreeUtils;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import com.spms.wms.entity.InventoryEntity;
import com.spms.wms.entity.StorageEntity;
import com.spms.wms.mapper.InventoryMapper;
import com.spms.wms.mapper.StorageMapper;
import com.spms.wms.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class StorageServiceImpl extends BaseService<StorageEntity> implements StorageService {

    private final StorageMapper storageMapper;
    private final InventoryMapper inventoryMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public List<StorageEntity> getList() {
        return TreeUtils.buildTree(
                storageMapper.selectList(null),
                StorageEntity::getId,
                StorageEntity::getParentId,
                StorageEntity::setChildren
        );
    }

    @Override
    public void add(StorageEntity request) {
        if (request.getCode() == null) {
            request.setCode(codeRuleService.createCode(CodeRuleField.STORAGE_CODE));
        }
        checkDuplicate(request.getName(), request.getCode(), null);
        storageMapper.insert(request);
    }

    @Override
    public void updateById(StorageEntity request) {
        if (request.getCode() == null) {
            request.setCode(codeRuleService.createCode(CodeRuleField.STORAGE_CODE));
        }
        checkDuplicate(request.getName(), request.getCode(), request.getId());
        storageMapper.updateById(request);
    }

    @Override
    public StorageEntity getById(IdRequest request) {
        return storageMapper.selectById(request.id());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdRequest request) {
        StorageEntity exist = storageMapper.selectById(request.id());
        if (exist == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "仓库不存在");
        }
        long children = storageMapper.selectCount(
                Wrappers.<StorageEntity>lambdaQuery().eq(StorageEntity::getParentId, request.id()));
        if (children > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "存在子仓库，无法删除");
        }
        long inventory = inventoryMapper.selectCount(
                Wrappers.<InventoryEntity>lambdaQuery().eq(InventoryEntity::getStorageId, request.id()));
        if (inventory > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "仓库存在库存记录，无法删除");
        }
        storageMapper.deleteById(request.id());
    }

    private void checkDuplicate(String name, String code,  Long excludeId) {
        LambdaQueryWrapper<StorageEntity> wrapper = Wrappers.lambdaQuery(StorageEntity.class)
                .and(query -> query.eq(StorageEntity::getName, name)
                        .or().eq(StorageEntity::getCode, code));
        if (excludeId != null) {
            wrapper.ne(StorageEntity::getId, excludeId);
        }
        if (storageMapper.selectCount(wrapper) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "仓库名称、编码已存在");
        }
    }
}
