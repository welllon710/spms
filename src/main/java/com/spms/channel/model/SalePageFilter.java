package com.spms.channel.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.spms.channel.entity.CustomerEntity;
import com.spms.channel.entity.SaleDetailEntity;

import java.util.List;

public record SalePageFilter(
        String billCode,
        String status,
        String reason,
        Long customerId,
        String customerName,
        String customerCode,
        CustomerEntity customer,
        List<SaleDetailEntity> details,
        @JsonAlias("detailList") List<SaleDetailEntity> detailList
) {
}
