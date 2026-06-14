package com.spms.channel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.spms.base.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("purchase")
public class PurchaseEntity extends BaseEntity {
    private String rejectReason;
    private String billCode;
    private String reason;
    private Integer status;
    private java.math.BigDecimal totalPrice;
    private java.math.BigDecimal totalRealPrice;

    @Valid
    @NotEmpty(message = "采购明细不能为空")
    @TableField(exist = false)
    private List<PurchaseDetailEntity> details;
}
