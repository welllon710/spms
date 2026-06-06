package com.spms.base;

import com.spms.common.config.AppProperties;
import com.spms.common.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ApiController {
    private final AppProperties appProperties;
    private final TokenService tokenService;
    private final HttpServletRequest request;

    protected long getCurrentUserId() {
        String token = request.getHeader(appProperties.getLoginHeader());
        return tokenService.verify(token);
    }
}
