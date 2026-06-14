package com.spms.channel.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PurchaseFinishRequest(
        @Positive(message = "采购明细ID必须大于0") Long id,
        @Positive(message = "采购单ID必须大于0") Long billId,
        @NotNull(message = "采购数量不能为空")
        @DecimalMin(value = "0.01", message = "采购数量必须大于0") BigDecimal quantity
) {}
