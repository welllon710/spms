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
@TableName("move_detail")
public class MoveDetailEntity extends BaseEntity {
    private Long billId;
    private Long inventoryId;
    private Double quantity;
    private Double finishQuantity;
    private Boolean isFinished;

    @TableField(exist = false)
    private InventoryEntity inventory;

    @TableField(exist = false)
    private MaterialEntity material;
}
