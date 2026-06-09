package com.spms.common.exception;

import com.spms.common.result.Json;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Set;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String MESSAGE_AND_DESCRIPTION = "%s (%s)";

    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void handleClientAbort(Exception exception) {
        log.debug("Client aborted request: {}", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Json<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        log.warn(exception.getMessage(), exception);
        BindingResult result = exception.getBindingResult();
        List<FieldError> errors = result.getFieldErrors();
        if (errors.isEmpty()) {
            return Json.error(CommonError.PARAM_INVALID);
        }
        FieldError first = errors.get(0);
        return Json.error(CommonError.PARAM_INVALID, String.format(
                MESSAGE_AND_DESCRIPTION, first.getDefaultMessage(), first.getField()
        )).setData(errors.stream()
                .map(error -> String.format(MESSAGE_AND_DESCRIPTION, error.getDefaultMessage(), error.getField()))
                .toList());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Json<Object> handleConstraintViolation(ConstraintViolationException exception) {
        log.warn(exception.getMessage(), exception);
        Set<ConstraintViolation<?>> errors = exception.getConstraintViolations();
        return errors.stream()
                .findFirst()
                .map(error -> Json.error(CommonError.PARAM_INVALID, String.format(
                        MESSAGE_AND_DESCRIPTION, error.getMessage(), error.getInvalidValue()
                )))
                .orElseGet(() -> Json.error(CommonError.PARAM_INVALID));
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public Json<Object> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.FORBIDDEN_DELETE_USED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Json<Object> handleNoHandler(NoHandlerFoundException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.API_SERVICE_UNSUPPORTED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Json<Object> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.REQUEST_CONTENT_TYPE_UNSUPPORTED, "请求参数格式不正确，请检查是否为合法 JSON");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Json<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        log.warn(exception.getMessage(), exception);
        String supported = exception.getSupportedMethods() == null ? "" : String.join("/", exception.getSupportedMethods());
        return Json.error(CommonError.REQUEST_METHOD_UNSUPPORTED,
                exception.getMethod() + " 不被支持，请使用 " + supported + " 方法请求");
    }

    @ExceptionHandler(MultipartException.class)
    public Json<Object> handleMultipart(MultipartException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.REQUEST_METHOD_UNSUPPORTED, "请使用 multipart 方式上传文件");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public Json<Object> handleMissingServletRequestPart(MissingServletRequestPartException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.PARAM_MISSING, "缺少文件 " + exception.getRequestPartName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Json<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.PARAM_MISSING, "缺少参数 " + exception.getParameterName());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Json<Object> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.REQUEST_CONTENT_TYPE_UNSUPPORTED, "请求内容类型不支持，请使用 JSON 请求");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Json<Object> handleUploadTooLarge(MaxUploadSizeExceededException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(CommonError.FORBIDDEN_UPLOAD_MAX_SIZE);
    }

    @ExceptionHandler({CannotCreateTransactionException.class, InvalidDataAccessResourceUsageException.class, CannotAcquireLockException.class, DataAccessException.class})
    public Json<Object> handleDataAccess(Exception exception) {
        log.error(exception.getMessage(), exception);
        return Json.error(CommonError.DATABASE_ERROR);
    }

    @ExceptionHandler(RedisConnectionFailureException.class)
    public Json<Object> handleRedisConnection(RedisConnectionFailureException exception) {
        log.error(exception.getMessage(), exception);
        return Json.error(CommonError.REDIS_ERROR);
    }

    @ExceptionHandler(AppException.class)
    public Json<Object> handleAppException(AppException exception) {
        log.warn(exception.getMessage(), exception);
        return Json.error(exception.getResultCode(), exception.getMessage()).setData(exception.getData());
    }

    @ExceptionHandler(Exception.class)
    public Json<Object> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return Json.error(CommonError.SERVICE_ERROR);
    }
}
