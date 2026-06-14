package com.spms.channel.model;

import com.spms.channel.entity.PurchaseDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PurchaseUpdateRequest(
        @Positive(message = "id必须大于0") Long id,
        String billCode,
        String reason,
        @NotEmpty(message = "采购明细不能为空") List<@Valid PurchaseDetailEntity> details
) {}
