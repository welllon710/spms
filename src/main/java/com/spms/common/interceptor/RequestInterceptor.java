package com.spms.common.interceptor;

import com.spms.common.config.AppProperties;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.security.Access;
import com.spms.common.security.LoginSessionService;
import com.spms.common.security.PermissionUtil;
import com.spms.common.security.TokenService;
import com.spms.personal.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestInterceptor implements HandlerInterceptor {
    private final AppProperties appProperties;
    private final TokenService tokenService;
    private final LoginSessionService loginSessionService;
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Access access = PermissionUtil.getRequiredAccess(handlerMethod.getBeanType(), handlerMethod.getMethod());
        if (!access.isLogin()) {
            return true;
        }
        String token = request.getHeader(appProperties.getLoginHeader());
        if (token == null || token.isBlank()) {
            throw new AppException(CommonError.UNAUTHORIZED);
        }
        long userId = tokenService.verify(token);
        loginSessionService.verify(userId, token);
        if (access.isAuthorize()) {
            checkUserPermission(userId, PermissionUtil.getPermissionIdentity(handlerMethod.getBeanType(), handlerMethod.getMethod()));
        }
        return true;
    }

    private void checkUserPermission(long userId, String permissionIdentity) {
        if (userId == 1L) {
            return;
        }
        List<String> permissionIdentityList = userService.getMyPermissionList(userId);
        if (permissionIdentityList != null && permissionIdentityList.contains(permissionIdentity)) {
            return;
        }
        throw new AppException(CommonError.FORBIDDEN, "你无权访问 " + permissionIdentity);
    }
}
