package com.spms.common.security;

import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class PermissionUtil {
    private static final String CONTROLLER = "Controller";

    private PermissionUtil() {
    }

    public static Access getRequiredAccess(Class<?> controllerClass, Method method) {
        Access access = new Access();
        Permission classPermission = controllerClass.getAnnotation(Permission.class);
        if (Objects.nonNull(classPermission)) {
            access.setLogin(classPermission.login());
            access.setAuthorize(classPermission.login() && classPermission.authorize());
        }
        Permission methodPermission = method.getAnnotation(Permission.class);
        if (Objects.nonNull(methodPermission)) {
            access.setLogin(methodPermission.login());
            access.setAuthorize(methodPermission.login() && methodPermission.authorize());
        }
        if (method.isAnnotationPresent(AuthIgnore.class) || controllerClass.isAnnotationPresent(AuthIgnore.class)) {
            access.setLogin(false).setAuthorize(false);
        }
        return access;
    }

    public static String getPermissionIdentity(Class<?> controllerClass, Method method) {
        String controllerName = controllerClass.getSimpleName().replace(CONTROLLER, "");
        if (controllerName.isEmpty()) {
            return method.getName();
        }
        return Character.toLowerCase(controllerName.charAt(0)) + controllerName.substring(1) + "_" + method.getName();
    }

    /**
     * 使用明文密码和盐生成密码摘要。
     */
    public static String encodePassword(String password, String salt) {
        if (password == null || password.isBlank()) {
            throw new AppException(CommonError.PARAM_MISSING, "密码不能为空");
        }
        if (salt == null || salt.isBlank()) {
            throw new AppException(CommonError.PARAM_MISSING, "盐不能为空");
        }
        return sha1Hex(sha1Hex(password + salt) + sha1Hex(salt + password));
    }

    private static String sha1Hex(String source) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(messageDigest.digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("不支持的摘要算法: SHA-1", exception);
        }
    }
}
