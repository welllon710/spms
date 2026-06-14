package com.spms.common.util;

import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

public final class ParamUtils {
    private ParamUtils() {
    }

    public static void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new AppException(CommonError.PARAM_MISSING, message);
        }
    }

    public static void requireId(Long id, String message) {
        requireNotNull(id, message);
        if (id <= 0) {
            throw new AppException(CommonError.PARAM_INVALID, message);
        }
    }

    public static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(CommonError.PARAM_MISSING, message);
        }
    }

    public static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    public static BigDecimal requirePositiveQuantity(BigDecimal value, String message) {
        requireNotNull(value, message);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(CommonError.PARAM_INVALID, message);
        }
        return value;
    }
}
