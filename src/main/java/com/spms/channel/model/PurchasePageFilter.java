package com.spms.channel.model;

import com.spms.channel.entity.PurchaseDetailEntity;
import com.spms.channel.entity.PurchaseEntity;

import java.util.List;

public record PurchasePageFilter(
        String purchaseType,
        String billCode,
        String status,
        String reason,
        List<PurchaseDetailEntity> details
        )  {
}
