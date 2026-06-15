package com.spms.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.asset.entity.MaterialEntity;
import com.spms.asset.mapper.MaterialMapper;
import com.spms.asset.model.MaterialPageFilter;
import com.spms.asset.service.MaterialService;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.base.SortParam;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl extends BaseService<MaterialEntity> implements MaterialService {
    private static final SortParam DEFAULT_SORT = new SortParam("id", "desc");

    private final MaterialMapper materialMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<MaterialEntity> getPage(PageQuery<MaterialPageFilter> request) {
        MaterialPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("name", MaterialPageFilter::name)
                .putTrim("code", MaterialPageFilter::code)
                .putTrim("spc", MaterialPageFilter::spc)
                .put("materialType", MaterialPageFilter::materialType)
                .put("useType", MaterialPageFilter::useType)
                .put("unitId", MaterialPageFilter::unitId)
                .put("isDisabled", MaterialPageFilter::isDisabled)
                .toMap();
        Page<MaterialEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(materialMapper.getPageList(page, params), DEFAULT_SORT);
    }

    @Override
    public MaterialEntity getDetail(Long id) {
        return getRequiredMaterial(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialEntity add(MaterialEntity material) {
        validateMaterial(material, false);
        checkDuplicate(material.getName(), material.getCode(), null);
        materialMapper.insert(material);
        return material;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialEntity update(MaterialEntity material) {
        validateMaterial(material, true);
        MaterialEntity exist = getRequiredMaterial(material.getId());
        checkEditable(exist);
        checkDuplicate(material.getName(), material.getCode(), material.getId());
        if (material.getIsDisabled() == null) {
            material.setIsDisabled(exist.getIsDisabled());
        }
        materialMapper.updateById(material);
        return getDetail(material.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MaterialEntity exist = getRequiredMaterial(id);
        checkEditable(exist);
        materialMapper.deleteById(id);
    }

    private MaterialEntity getRequiredMaterial(Long id) {
        requireId(id, "物料ID不能为空");
        MaterialEntity material = materialMapper.getById(id);
        if (material == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "物料不存在");
        }
        return material;
    }

    private void validateMaterial(MaterialEntity material, boolean requireId) {
        requireNotNull(material, "请求参数不能为空");
        if (requireId) {
            requireId(material.getId(), "物料ID不能为空");
        }
        material.setName(trimToNull(material.getName()));
        material.setCode(trimToNull(material.getCode()));
        material.setSpc(trimToNull(material.getSpc()));
        requireText(material.getName(), "物料名称不能为空");
        requireNotNull(material.getUnit(), "物料单位不能为空");
        if (material.getCode() == null) {
            material.setCode(codeRuleService.createCode(CodeRuleField.MATERIAL_CODE));
        }
        if (material.getPurchasePrice() == null) {
            material.setPurchasePrice(BigDecimal.ZERO);
        }
        if (material.getSalePrice() == null) {
            material.setSalePrice(BigDecimal.ZERO);
        }
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        LambdaQueryWrapper<MaterialEntity> wrapper = Wrappers.lambdaQuery(MaterialEntity.class)
                .and(query -> query.eq(MaterialEntity::getName, name).or().eq(MaterialEntity::getCode, code));
        if (excludeId != null) {
            wrapper.ne(MaterialEntity::getId, excludeId);
        }
        if (materialMapper.selectCount(wrapper) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "物料名称或编码已存在");
        }
    }

}
