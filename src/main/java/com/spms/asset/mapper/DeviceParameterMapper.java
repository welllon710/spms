package com.spms.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.asset.entity.DeviceParameterEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceParameterMapper extends BaseMapper<DeviceParameterEntity> {
    default long countByParameterId(Long parameterId) {
        return selectCount(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<DeviceParameterEntity>lambdaQuery()
                        .eq(DeviceParameterEntity::getParameterId, parameterId)
        );
    }
}
