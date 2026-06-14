package com.spms.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("inventory")
public class InventoryEntity extends BaseEntity {
    private Long materialId;
    private java.math.BigDecimal quantity;
    private Integer type;
    private Long storageId;
    private Long structureId;

    @TableField(exist = false)
    private MaterialEntity material;

    @TableField(exist = false)
    private StorageEntity storage;
}
