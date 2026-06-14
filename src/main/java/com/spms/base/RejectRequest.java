package com.spms.base;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RejectRequest(
        @Positive(message = "id必须大于0") Long id,
        @NotBlank(message = "驳回原因不能为空") String rejectReason
) {}
