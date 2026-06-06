package com.spms.common.exception;

import com.spms.common.result.ResultCode;
import lombok.Getter;

@Getter
public enum CommonError implements ResultCode {
    PARAM_MISSING(4001, "缺少必要的请求参数"),
    PARAM_INVALID(4002, "请求的参数校验失败"),
    UNAUTHORIZED(401, "获取你的身份信息失败，请重新登录后再试"),
    FORBIDDEN(403, "无权限操作"),
    FORBIDDEN_DELETE_USED(4034, "删除失败，数据正在使用中"),
    FORBIDDEN_UPLOAD_MAX_SIZE(4035, "上传的文件大小超过最大限制"),
    DATA_NOT_FOUND(404, "没有查到相关的数据"),
    REQUEST_METHOD_UNSUPPORTED(405, "不支持的请求方法"),
    REQUEST_CONTENT_TYPE_UNSUPPORTED(415, "不支持的数据类型"),
    SERVICE_ERROR(500, "服务出了一点点异常，请稍后再试"),
    API_SERVICE_UNSUPPORTED(501, "请求的接口暂未实现"),
    DATABASE_ERROR(5021, "数据库服务连接失败，请稍后再试"),
    REDIS_ERROR(5022, "REDIS服务连接失败，请稍后再试");

    private final int code;
    private final String message;

    CommonError(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
