package com.spms.personal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.base.SortParam;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.util.QueryParams;
import com.spms.personal.entity.UnitEntity;
import com.spms.personal.mapper.UnitMapper;
import com.spms.personal.model.UnitPageFilter;
import com.spms.asset.entity.MaterialEntity;
import com.spms.asset.mapper.MaterialMapper;
import com.spms.personal.service.UnitService;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl extends BaseService<UnitEntity> implements UnitService {
    private static final SortParam DEFAULT_SORT = new SortParam("id", "desc");

    private final UnitMapper unitMapper;
    private final MaterialMapper materialMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<UnitEntity> getPage(PageQuery<UnitPageFilter> request) {
        UnitPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("name", UnitPageFilter::name)
                .putTrim("code", UnitPageFilter::code)
                .put("isDisabled", UnitPageFilter::isDisabled)
                .toMap();
        Page<UnitEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(unitMapper.getPageList(page, params), DEFAULT_SORT);
    }

    @Override
    public UnitEntity getDetail(Long id) {
        return getRequiredUnit(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnitEntity add(UnitEntity unit) {
        validateUnit(unit, false);
        checkDuplicate(unit.getName(), unit.getCode(), null);
        unitMapper.insert(unit);
        return unit;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnitEntity update(UnitEntity unit) {
        validateUnit(unit, true);
        UnitEntity exist = getRequiredUnit(unit.getId());
        checkEditable(exist);
        checkDuplicate(unit.getName(), unit.getCode(), unit.getId());
        if (unit.getIsDisabled() == null) {
            unit.setIsDisabled(exist.getIsDisabled());
        }
        unitMapper.update(unit);
        return getDetail(unit.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        UnitEntity exist = getRequiredUnit(id);
        checkEditable(exist);
        long count = materialMapper.selectCount(
                Wrappers.<MaterialEntity>lambdaQuery()
                        .eq(MaterialEntity::getUnitId, id));
        if (count > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "单位已被物料引用，无法删除");
        }
        unitMapper.deleteById(id);
    }

    private UnitEntity getRequiredUnit(Long id) {
        requireId(id, "单位ID不能为空");
        UnitEntity unit = unitMapper.getById(id);
        if (unit == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "单位不存在");
        }
        return unit;
    }

    private void validateUnit(UnitEntity unit, boolean requireId) {
        requireNotNull(unit, "请求参数不能为空");
        if (requireId && unit.getId() == null) {
            throw new AppException(CommonError.PARAM_MISSING, "单位ID不能为空");
        }
        unit.setName(trimToNull(unit.getName()));
        unit.setCode(trimToNull(unit.getCode()));
        requireText(unit.getName(), "单位名称不能为空");
        if (unit.getCode() == null) {
            unit.setCode(codeRuleService.createCode(CodeRuleField.UNIT_CODE));
        }
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        if (unitMapper.countByNameOrCode(name, code, excludeId) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "单位名称或编码已存在");
        }
    }
}
