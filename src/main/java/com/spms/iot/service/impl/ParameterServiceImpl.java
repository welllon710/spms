package com.spms.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.asset.mapper.DeviceParameterMapper;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.base.SortParam;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.iot.entity.ParameterEntity;
import com.spms.iot.mapper.ParameterMapper;
import com.spms.iot.model.ParameterPageFilter;
import com.spms.iot.service.ParameterService;
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
public class ParameterServiceImpl extends BaseService<ParameterEntity> implements ParameterService {
    private static final SortParam DEFAULT_SORT = new SortParam("id", "desc");

    private final ParameterMapper parameterMapper;
    private final DeviceParameterMapper deviceParameterMapper;

    @Override
    public PageResult<ParameterEntity> getPage(PageQuery<ParameterPageFilter> request) {
        ParameterPageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("code", ParameterPageFilter::code)
                .putTrim("label", ParameterPageFilter::label)
                .put("isSystem", ParameterPageFilter::isSystem)
                .put("dataType", ParameterPageFilter::dataType)
                .put("isDisabled", ParameterPageFilter::isDisabled)
                .toMap();
        Page<ParameterEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(parameterMapper.selectPage(page, buildPageWrapper(params)), DEFAULT_SORT);
    }

    @Override
    public ParameterEntity getDetail(Long id) {
        return getRequiredParameter(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParameterEntity add(ParameterEntity parameter) {
        validateParameter(parameter, false);
        initAddEntity(parameter);
        parameter.setIsSystem(Boolean.TRUE.equals(parameter.getIsSystem()));
        checkDuplicate(parameter.getCode(), parameter.getLabel(), null);
        parameterMapper.insert(parameter);
        return parameter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParameterEntity update(ParameterEntity parameter) {
        validateParameter(parameter, true);
        ParameterEntity exist = getRequiredParameter(parameter.getId());
        checkEditable(exist);
        checkDuplicate(parameter.getCode(), parameter.getLabel(), parameter.getId());
        initUpdateEntity(parameter);
        if (parameter.getIsDisabled() == null) {
            parameter.setIsDisabled(exist.getIsDisabled());
        }
        if (parameter.getIsSystem() == null) {
            parameter.setIsSystem(exist.getIsSystem());
        }
        parameterMapper.updateById(parameter);
        return getDetail(parameter.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParameterEntity exist = getRequiredParameter(id);
        checkEditable(exist);
        if (Boolean.TRUE.equals(exist.getIsSystem())) {
            throw new AppException(CommonError.FORBIDDEN, "系统参数不能删除");
        }
        if (deviceParameterMapper.countByParameterId(id) > 0) {
            throw new AppException(CommonError.FORBIDDEN_DELETE_USED, "参数正在被设备使用，不能删除");
        }
        parameterMapper.deleteById(id);
    }

    private ParameterEntity getRequiredParameter(Long id) {
        requireId(id, "参数ID不能为空");
        ParameterEntity parameter = parameterMapper.selectById(id);
        if (parameter == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "参数不存在");
        }
        return parameter;
    }

    private void validateParameter(ParameterEntity parameter, boolean requireId) {
        requireNotNull(parameter, "请求参数不能为空");
        if (requireId) {
            requireId(parameter.getId(), "参数ID不能为空");
        }
        parameter.setCode(trimToNull(parameter.getCode()));
        parameter.setLabel(trimToNull(parameter.getLabel()));
        requireText(parameter.getCode(), "参数编码不能为空");
        requireText(parameter.getLabel(), "参数名称不能为空");
    }

    private void checkDuplicate(String code, String label, Long excludeId) {
        LambdaQueryWrapper<ParameterEntity> wrapper = Wrappers.lambdaQuery(ParameterEntity.class)
                .and(query -> query.eq(ParameterEntity::getCode, code).or().eq(ParameterEntity::getLabel, label));
        if (excludeId != null) {
            wrapper.ne(ParameterEntity::getId, excludeId);
        }
        if (parameterMapper.selectCount(wrapper) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "参数编码或名称已存在");
        }
    }

    private LambdaQueryWrapper<ParameterEntity> buildPageWrapper(Map<String, Object> params) {
        String code = (String) params.get("code");
        String label = (String) params.get("label");
        Boolean isSystem = (Boolean) params.get("isSystem");
        Integer dataType = (Integer) params.get("dataType");
        Boolean isDisabled = (Boolean) params.get("isDisabled");
        return Wrappers.lambdaQuery(ParameterEntity.class)
                .like(code != null, ParameterEntity::getCode, code)
                .like(label != null, ParameterEntity::getLabel, label)
                .eq(isSystem != null, ParameterEntity::getIsSystem, isSystem)
                .eq(dataType != null, ParameterEntity::getDataType, dataType)
                .eq(isDisabled != null, ParameterEntity::getIsDisabled, isDisabled)
                .orderByDesc(ParameterEntity::getId);
    }
}
