package com.spms.wms.model;

import com.spms.wms.entity.StorageEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InputFinishRequest(
        @Positive(message = "入库明细ID必须大于0") Long id,
        @NotNull(message = "入库数量不能为空")
        @DecimalMin(value = "0.01", message = "入库数量必须大于0") BigDecimal quantity,
        Long storageId,
        StorageEntity storage
) {}
