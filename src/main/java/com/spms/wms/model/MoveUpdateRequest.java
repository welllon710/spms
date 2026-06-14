package com.spms.wms.model;

import com.spms.wms.entity.MoveDetailEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record MoveUpdateRequest(
        @Positive(message = "id必须大于0") Long id,
        String billCode,
        @NotNull(message = "目标仓库不能为空") Long storageId,
        @NotEmpty(message = "移库明细不能为空") List<@Valid MoveDetailEntity> details
) {}
