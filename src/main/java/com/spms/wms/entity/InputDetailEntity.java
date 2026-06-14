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
@TableName("input_detail")
public class InputDetailEntity extends BaseEntity {
    private Long billId;
    private Boolean isFinished;
    private java.math.BigDecimal finishQuantity;
    @NotNull(message = "入库数量不能为空")
    @DecimalMin(value = "0.01", message = "入库数量必须大于0")
    private java.math.BigDecimal quantity;
    private Long materialId;

    @TableField(exist = false)
    private MaterialEntity material;
}
