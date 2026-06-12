package com.spms.asset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spms.asset.entity.DeviceEntity;
import com.spms.iot.entity.ParameterEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceMapper extends BaseMapper<DeviceEntity> {
    List<ParameterEntity> getParameterListByDeviceId(@Param("deviceId") Long deviceId);
}
