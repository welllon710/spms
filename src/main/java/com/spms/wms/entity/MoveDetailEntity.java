package com.spms.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseEntity;
import java.math.BigDecimal;
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
    @NotNull(message = "移库数量不能为空")
    @DecimalMin(value = "0.01", message = "移库数量必须大于0")
    private BigDecimal quantity;
    private BigDecimal finishQuantity;
    private Boolean isFinished;

    @TableField(exist = false)
    private InventoryEntity inventory;

    @TableField(exist = false)
    private MaterialEntity material;
}
