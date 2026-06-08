package com.spms.personal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.common.result.PageResult;
import com.spms.base.SortParam;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.personal.entity.UnitEntity;
import com.spms.personal.mapper.UnitMapper;
import com.spms.personal.model.UnitPageFilter;
import com.spms.personal.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

    @Override
    public PageResult<UnitEntity> getPage(PageQuery<UnitPageFilter> request) {
        UnitPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = new HashMap<>();
        params.put("name", trimToNull(filter == null ? null : filter.name()));
        params.put("code", trimToNull(filter == null ? null : filter.code()));
        params.put("isDisabled", filter == null ? null : filter.isDisabled());
        PageHelper.startPage(getPageNum(request), getPageSize(request));
        return PageResult.from(new PageInfo<>(unitMapper.getPageList(params)), DEFAULT_SORT);
    }

    @Override
    public UnitEntity getDetail(Long id) {
        return getRequiredUnit(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UnitEntity add(UnitEntity unit) {
        validateUnit(unit, false);
        initAddEntity(unit);
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
        initUpdateEntity(unit);
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
        requireText(unit.getCode(), "单位编码不能为空");
    }

    private void checkDuplicate(String name, String code, Long excludeId) {
        if (unitMapper.countByNameOrCode(name, code, excludeId) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "单位名称或编码已存在");
        }
    }
}
