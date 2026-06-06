package com.spms.common.exception;

import com.spms.common.result.ResultCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ResultCode resultCode;
    private final Object data;

    public AppException(ResultCode resultCode) {
        this(resultCode, resultCode.getMessage(), null);
    }

    public AppException(ResultCode resultCode, String message) {
        this(resultCode, message, null);
    }

    public AppException(ResultCode resultCode, String message, Object data) {
        super(message);
        this.resultCode = resultCode;
        this.data = data;
    }
}
