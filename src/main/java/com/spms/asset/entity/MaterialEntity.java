package com.spms.asset.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import com.spms.personal.entity.UnitEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("material")
public class MaterialEntity extends BaseEntity {
    private String code;
    private String name;
    private String spc;
    private Long materialType;
    private Long useType;
    private Long unitId;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;

    @TableField(exist = false)
    private UnitEntity unit;
}
