package com.spms.wms.model;

import com.spms.wms.entity.OutputDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OutputUpdateRequest(
        @Positive(message = "id必须大于0") Long id,
        String billCode,
        Integer type,
        Long moveId,
        Long pickingId,
        Long saleId,
        @NotEmpty(message = "出库明细不能为空") List<@Valid OutputDetailEntity> details
) {}
