package com.spms.wms.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MoveFinishRequest(
        @Positive(message = "移库明细ID必须大于0") Long id,
        @NotNull(message = "移库数量不能为空")
        @DecimalMin(value = "0.01", message = "移库数量必须大于0") BigDecimal quantity
) {}
