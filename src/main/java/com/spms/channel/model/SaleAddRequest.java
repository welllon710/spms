package com.spms.channel.model;

import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.SaleDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaleAddRequest(
        String billCode,
        String reason,
        Long customerId,
        CustomerEntity customer,
        @NotEmpty(message = "销售明细不能为空") List<@Valid SaleDetailEntity> details
) {}
