package com.spms.base;

import com.spms.common.config.AppProperties;
import com.spms.common.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public abstract class ApiController {

    @Autowired
    private  AppProperties appProperties;
    @Autowired
    private  TokenService tokenService;
    @Autowired
    private  HttpServletRequest request;

    protected String getLoginToken() {
        return request.getHeader(appProperties.getLoginHeader());
    }

    protected long getCurrentUserId() {
        return tokenService.verify(getLoginToken());
    }
}
