package com.spms.base;

import jakarta.validation.constraints.Positive;

public record IdRequest(
        @Positive(message = "id必须大于0") Long id
) {}
