package com.spms.asset.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import com.spms.iot.entity.ParameterEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("device")
public class DeviceEntity extends BaseEntity {
    private String name;
    private String code;
    private String uuid;
    private Integer status;
    private Integer alarm;
    private Long partCount;
    private Boolean isReporting;
    private Integer rate;

    @TableField(exist = false)
    private List<ParameterEntity> parameters;
}
