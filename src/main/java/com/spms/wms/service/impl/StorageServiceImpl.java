package com.spms.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.spms.asset.entity.DeviceEntity;
import com.spms.base.BaseService;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.util.ParamUtils;
import com.spms.common.util.TreeUtils;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.mapper.CodeRuleMapper;
import com.spms.system.service.CodeRuleService;
import com.spms.wms.entity.StorageEntity;
import com.spms.wms.mapper.StorageMapper;
import com.spms.wms.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireNotNull;

@Service
@AllArgsConstructor
public class StorageServiceImpl extends BaseService<StorageEntity> implements StorageService {

    private final StorageMapper storageMapper;

    private final CodeRuleService codeRuleService;

    @Override
    public List<StorageEntity> getList() {
        return TreeUtils.buildTree(
                storageMapper.selectList(new QueryWrapper<>()),
                StorageEntity::getId,
                StorageEntity::getParentId,
                StorageEntity::setChildren
        );
    }

    @Override
    public void add(StorageEntity request) {
        requireNotNull(request, "请求参数不能为空");
        if (request.getCode() == null) {
            request.setCode(codeRuleService.createCode(CodeRuleField.STORAGE_CODE));
        }
        initAddEntity(request);
        checkDuplicate(request.getName(), request.getCode(), null);
        storageMapper.insert(request);
    }

    @Override
    public void updateById(StorageEntity request) {
        requireNotNull(request, "请求参数不能为空");
        if (request.getCode() == null) {
            request.setCode(codeRuleService.createCode(CodeRuleField.STORAGE_CODE));
        }
        initUpdateEntity(request);
        checkDuplicate(request.getName(), request.getCode(), null);
        storageMapper.updateById(request);
    }

    @Override
    public StorageEntity getById(Map<String, String> map) {
        requireNotNull(map, "请求参数不能为空");
        ParamUtils.requireText(map.get("id"), "id 不能为空");
        return storageMapper.selectById(map.get("id"));
    }

    private void checkDuplicate(String name, String code,  Long excludeId) {
        LambdaQueryWrapper<StorageEntity> wrapper = Wrappers.lambdaQuery(StorageEntity.class)
                .and(query -> query.eq(StorageEntity::getName, name)
                        .or().eq(StorageEntity::getCode, code));
//                        .or().eq(StorageEntity::getUuid, uuid));
        if (excludeId != null) {
            wrapper.ne(StorageEntity::getId, excludeId);
        }
        if (storageMapper.selectCount(wrapper) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "仓库名称、编码已存在");
        }
    }
}
