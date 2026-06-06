package com.spms.common.security;

import java.lang.reflect.Method;
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
}
