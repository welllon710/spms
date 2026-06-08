package com.spms.common.interceptor;

import com.spms.common.config.AppProperties;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.security.LoginSessionService;
import com.spms.common.security.Permission;
import com.spms.common.security.TokenService;
import com.spms.personal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestInterceptorTest {
    private static final String TOKEN = "token";

    @Mock
    private TokenService tokenService;

    @Mock
    private LoginSessionService loginSessionService;

    @Mock
    private UserService userService;

    private RequestInterceptor requestInterceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        AppProperties appProperties = new AppProperties();
        appProperties.setLoginHeader("Authorization");
        requestInterceptor = new RequestInterceptor(appProperties, tokenService, loginSessionService, userService);

        request = new MockHttpServletRequest();
        request.addHeader("Authorization", TOKEN);
        response = new MockHttpServletResponse();

        Method method = ProtectedController.class.getMethod("getPage");
        handlerMethod = new HandlerMethod(new ProtectedController(), method);
    }

    @Test
    void allowsUserWhenPermissionIdentityExists() {
        when(tokenService.verify(TOKEN)).thenReturn(2L);
        when(userService.getMyPermissionList(2L)).thenReturn(List.of("protected_getPage"));

        boolean allowed = requestInterceptor.preHandle(request, response, handlerMethod);

        assertThat(allowed).isTrue();
        verify(loginSessionService).verify(2L, TOKEN);
        verify(userService).getMyPermissionList(2L);
    }

    @Test
    void rejectsUserWhenPermissionIdentityIsMissing() {
        when(tokenService.verify(TOKEN)).thenReturn(2L);
        when(userService.getMyPermissionList(2L)).thenReturn(List.of("role_getPage"));

        assertThatThrownBy(() -> requestInterceptor.preHandle(request, response, handlerMethod))
                .isInstanceOf(AppException.class)
                .extracting("resultCode")
                .isEqualTo(CommonError.FORBIDDEN);
    }

    @Test
    void skipsPermissionLookupForRootUser() {
        when(tokenService.verify(TOKEN)).thenReturn(1L);

        boolean allowed = requestInterceptor.preHandle(request, response, handlerMethod);

        assertThat(allowed).isTrue();
        verify(userService, never()).getMyPermissionList(1L);
    }

    @Permission
    static class ProtectedController {
        public void getPage() {
        }
    }
}
