package com.spms.wms.model;

import com.spms.wms.entity.InputDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record InputUpdateRequest(
        @Positive(message = "id必须大于0") Long id,
        String billCode,
        Integer type,
        Long moveId,
        Long orderId,
        Long purchaseId,
        @Positive(message = "移库单关联ID须大于0") Long structureId,
        @NotEmpty(message = "入库明细不能为空") List<@Valid InputDetailEntity> details
) {}
