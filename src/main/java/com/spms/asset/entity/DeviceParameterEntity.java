package com.spms.asset.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import com.spms.iot.entity.ParameterEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("device_parameter")
public class DeviceParameterEntity extends BaseEntity {
    private Long deviceId;
    private Long parameterId;

    @TableField(exist = false)
    private DeviceEntity device;

    @TableField(exist = false)
    private ParameterEntity parameter;
}
