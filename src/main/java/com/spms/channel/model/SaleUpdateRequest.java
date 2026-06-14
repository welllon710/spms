package com.spms.channel.model;

import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.SaleDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record SaleUpdateRequest(
        @Positive(message = "id必须大于0") Long id,
        String billCode,
        String reason,
        Long customerId,
        CustomerEntity customer,
        @NotEmpty(message = "销售明细不能为空") List<@Valid SaleDetailEntity> details
) {}
