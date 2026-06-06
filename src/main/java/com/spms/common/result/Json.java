package com.spms.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Json<T> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private int code;
    private String message;
    private T data;

    public static <T> Json<T> data(T data) {
        return data(data, "查询成功");
    }

    public static <T> Json<T> data(T data, String message) {
        return new Json<T>()
                .setCode(0)
                .setMessage(message)
                .setData(data);
    }

    public static <T> Json<T> success(String message) {
        return data(null, message);
    }

    public static <T> Json<T> error(String message) {
        return new Json<T>()
                .setCode(500)
                .setMessage(message);
    }

    public static <T> Json<T> error(ResultCode resultCode) {
        return error(resultCode, resultCode.getMessage());
    }

    public static <T> Json<T> error(ResultCode resultCode, String message) {
        return new Json<T>()
                .setCode(resultCode.getCode())
                .setMessage(message);
    }

    public static String toString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("JSON编码失败", exception);
        }
    }

    public static <T> T parse(String value, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(value, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("JSON解码失败", exception);
        }
    }

    public static <T> List<T> parseList(String value, Class<T[]> arrayType) {
        T[] array = parse(value, arrayType);
        return List.of(array);
    }
}
