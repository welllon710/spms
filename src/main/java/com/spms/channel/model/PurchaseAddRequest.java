package com.spms.channel.model;

import com.spms.channel.entity.PurchaseDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PurchaseAddRequest(
        String billCode,
        String reason,
        @NotEmpty(message = "采购明细不能为空") List<@Valid PurchaseDetailEntity> details
) {}
