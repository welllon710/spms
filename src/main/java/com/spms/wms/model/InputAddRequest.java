package com.spms.wms.model;

import com.spms.wms.entity.InputDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.List;

@Builder
public record InputAddRequest(
        String billCode,
        Integer type,
        Long moveId,
        Long orderId,
        Long purchaseId,
        Long structureId,
        @NotEmpty(message = "入库明细不能为空") List<@Valid InputDetailEntity> details
) {

}
