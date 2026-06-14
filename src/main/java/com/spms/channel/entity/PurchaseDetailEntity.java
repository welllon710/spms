package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseEntity;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("purchase_detail")
public class PurchaseDetailEntity extends BaseEntity {
    private Long billId;
    private Boolean isFinished;
    private java.math.BigDecimal finishQuantity;
    @NotNull(message = "采购单价不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "采购单价必须大于0")
    private java.math.BigDecimal price;
    @NotNull(message = "采购数量不能为空")
    @DecimalMin(value = "0.01", message = "采购数量必须大于0")
    private java.math.BigDecimal quantity;
    private Long materialId;
    private Long supplierId;

    @TableField(exist = false)
    private MaterialEntity material;

    @TableField(exist = false)
    private SupplierEntity supplier;
}
