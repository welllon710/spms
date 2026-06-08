package com.spms.common.util;

import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import org.springframework.util.StringUtils;

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
}
