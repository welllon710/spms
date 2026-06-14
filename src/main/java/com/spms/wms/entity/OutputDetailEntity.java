package com.spms.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
@TableName("output_detail")
public class OutputDetailEntity extends BaseEntity {
    private Long billId;
    private Boolean isFinished;
    private java.math.BigDecimal finishQuantity;
    @NotNull(message = "出库数量不能为空")
    @DecimalMin(value = "0.01", message = "出库数量必须大于0")
    private java.math.BigDecimal quantity;
    private Long inventoryId;
    private Long materialId;

    @TableField(exist = false)
    private MaterialEntity material;

    @TableField(exist = false)
    private InventoryEntity inventory;
}
