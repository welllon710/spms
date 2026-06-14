package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.asset.entity.MaterialEntity;
import com.spms.base.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("sale_detail")
public class SaleDetailEntity extends BaseEntity {
    private Long billId;
    private Boolean isFinished;
    private BigDecimal finishQuantity;
    @NotNull(message = "销售单价不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "销售单价必须大于0")
    private BigDecimal price;
    @NotNull(message = "销售数量不能为空")
    @DecimalMin(value = "0.01", message = "销售数量必须大于0")
    private BigDecimal quantity;
    private Long materialId;

    @TableField(exist = false)
    private MaterialEntity material;
}
