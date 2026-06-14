package com.spms.wms.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OutputFinishRequest(
        @Positive(message = "出库明细ID必须大于0") Long id,
        @NotNull(message = "出库数量不能为空")
        @DecimalMin(value = "0.01", message = "出库数量必须大于0") BigDecimal quantity
) {}
