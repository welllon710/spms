package com.spms.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spms.asset.entity.DeviceEntity;
import com.spms.asset.entity.DeviceParameterEntity;
import com.spms.asset.mapper.DeviceMapper;
import com.spms.asset.mapper.DeviceParameterMapper;
import com.spms.asset.model.DevicePageFilter;
import com.spms.asset.service.DeviceService;
import com.spms.base.BaseService;
import com.spms.base.PageQuery;
import com.spms.base.SortParam;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.PageResult;
import com.spms.common.util.QueryParams;
import com.spms.iot.entity.ParameterEntity;
import com.spms.system.enums.CodeRuleField;
import com.spms.system.service.CodeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.spms.common.util.ParamUtils.requireId;
import static com.spms.common.util.ParamUtils.requireNotNull;
import static com.spms.common.util.ParamUtils.requireText;
import static com.spms.common.util.ParamUtils.trimToNull;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends BaseService<DeviceEntity> implements DeviceService {
    private static final SortParam DEFAULT_SORT = new SortParam("id", "desc");
    private static final int DEFAULT_STATUS = 4;
    private static final int DEFAULT_ALARM = 0;
    private static final long DEFAULT_PART_COUNT = 0L;
    private static final boolean DEFAULT_IS_REPORTING = true;
    private static final int DEFAULT_RATE = 1000;

    private final DeviceMapper deviceMapper;
    private final DeviceParameterMapper deviceParameterMapper;
    private final CodeRuleService codeRuleService;

    @Override
    public PageResult<DeviceEntity> getPage(PageQuery<DevicePageFilter> request) {
        DevicePageFilter filter = request == null ? null : request.filter();
        Map<String, Object> params = QueryParams.of(filter)
                .putTrim("name", DevicePageFilter::name)
                .putTrim("code", DevicePageFilter::code)
                .putTrim("uuid", DevicePageFilter::uuid)
                .put("status", DevicePageFilter::status)
                .put("alarm", DevicePageFilter::alarm)
                .put("isReporting", DevicePageFilter::isReporting)
                .put("isDisabled", DevicePageFilter::isDisabled)
                .toMap();
        Page<DeviceEntity> page = new Page<>(getPageNum(request), getPageSize(request));
        return PageResult.from(deviceMapper.selectPage(page, buildPageWrapper(params)), DEFAULT_SORT);
    }

    @Override
    public DeviceEntity getDetail(Long id) {
        DeviceEntity device = getRequiredDevice(id);
        device.setParameters(deviceMapper.getParameterListByDeviceId(id));
        return device;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceEntity add(DeviceEntity device) {
        validateDevice(device, false);
        applyAddDefaults(device);
        checkDuplicate(device.getName(), device.getCode(), device.getUuid(), null);
        deviceMapper.insert(device);
        saveParameterRelations(device.getId(), device.getParameters());
        return getDetail(device.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceEntity update(DeviceEntity device) {
        validateDevice(device, true);
        DeviceEntity exist = getRequiredDevice(device.getId());
        checkEditable(exist);
        applyUpdateDefaults(device, exist);
        checkDuplicate(device.getName(), device.getCode(), device.getUuid(), device.getId());
        deviceMapper.updateById(device);
        if (device.getParameters() != null) {
            deleteParameterRelations(device.getId());
            saveParameterRelations(device.getId(), device.getParameters());
        }
        return getDetail(device.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DeviceEntity exist = getRequiredDevice(id);
        checkEditable(exist);
        deleteParameterRelations(id);
        deviceMapper.deleteById(id);
    }

    private DeviceEntity getRequiredDevice(Long id) {
        requireId(id, "设备ID不能为空");
        DeviceEntity device = deviceMapper.selectById(id);
        if (device == null) {
            throw new AppException(CommonError.DATA_NOT_FOUND, "设备不存在");
        }
        return device;
    }

    private void validateDevice(DeviceEntity device, boolean requireId) {
        requireNotNull(device, "请求参数不能为空");
        if (requireId) {
            requireId(device.getId(), "设备ID不能为空");
        }
        device.setName(trimToNull(device.getName()));
        device.setCode(trimToNull(device.getCode()));
        device.setUuid(trimToNull(device.getUuid()));
        requireText(device.getName(), "设备名称不能为空");
    }

    private void applyAddDefaults(DeviceEntity device) {
        if (device.getCode() == null) {
            device.setCode(codeRuleService.createCode(CodeRuleField.DEVICE_CODE));
        }
        if (device.getUuid() == null) {
            device.setUuid(device.getCode());
        }
        device.setStatus(DEFAULT_STATUS);
        device.setAlarm(DEFAULT_ALARM);
        device.setPartCount(DEFAULT_PART_COUNT);
        device.setIsReporting(device.getIsReporting() == null ? DEFAULT_IS_REPORTING : device.getIsReporting());
        device.setRate(device.getRate() == null ? DEFAULT_RATE : device.getRate());
    }

    private void applyUpdateDefaults(DeviceEntity device, DeviceEntity exist) {
        if (device.getCode() == null) {
            device.setCode(exist.getCode());
        }
        if (device.getUuid() == null) {
            device.setUuid(exist.getUuid() == null ? device.getCode() : exist.getUuid());
        }
        if (device.getIsDisabled() == null) {
            device.setIsDisabled(exist.getIsDisabled());
        }
        if (device.getIsReporting() == null) {
            device.setIsReporting(exist.getIsReporting());
        }
        if (device.getRate() == null) {
            device.setRate(exist.getRate());
        }
        device.setStatus(exist.getStatus());
        device.setAlarm(exist.getAlarm());
        device.setPartCount(exist.getPartCount());
    }

    private void checkDuplicate(String name, String code, String uuid, Long excludeId) {
        LambdaQueryWrapper<DeviceEntity> wrapper = Wrappers.lambdaQuery(DeviceEntity.class)
                .and(query -> query.eq(DeviceEntity::getName, name)
                        .or().eq(DeviceEntity::getCode, code)
                        .or().eq(DeviceEntity::getUuid, uuid));
        if (excludeId != null) {
            wrapper.ne(DeviceEntity::getId, excludeId);
        }
        if (deviceMapper.selectCount(wrapper) > 0) {
            throw new AppException(CommonError.PARAM_INVALID, "设备名称、编码或UUID已存在");
        }
    }

    private void saveParameterRelations(Long deviceId, List<ParameterEntity> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        for (ParameterEntity parameter : parameters) {
            requireNotNull(parameter, "设备参数不能为空");
            requireId(parameter.getId(), "设备参数ID不能为空");
            DeviceParameterEntity relation = new DeviceParameterEntity();
            relation.setDeviceEntityId(deviceId);
            relation.setParametersId(parameter.getId());
            deviceParameterMapper.insert(relation);
        }
    }

    private void deleteParameterRelations(Long deviceId) {
        deviceParameterMapper.delete(
                Wrappers.<DeviceParameterEntity>lambdaQuery()
                        .eq(DeviceParameterEntity::getDeviceEntityId, deviceId)
        );
    }

    private LambdaQueryWrapper<DeviceEntity> buildPageWrapper(Map<String, Object> params) {
        String name = (String) params.get("name");
        String code = (String) params.get("code");
        String uuid = (String) params.get("uuid");
        Integer status = (Integer) params.get("status");
        Integer alarm = (Integer) params.get("alarm");
        Boolean isReporting = (Boolean) params.get("isReporting");
        Boolean isDisabled = (Boolean) params.get("isDisabled");
        return Wrappers.lambdaQuery(DeviceEntity.class)
                .like(name != null, DeviceEntity::getName, name)
                .like(code != null, DeviceEntity::getCode, code)
                .like(uuid != null, DeviceEntity::getUuid, uuid)
                .eq(status != null, DeviceEntity::getStatus, status)
                .eq(alarm != null, DeviceEntity::getAlarm, alarm)
                .eq(isReporting != null, DeviceEntity::getIsReporting, isReporting)
                .eq(isDisabled != null, DeviceEntity::getIsDisabled, isDisabled)
                .orderByDesc(DeviceEntity::getId);
    }
}
