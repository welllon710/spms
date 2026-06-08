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

    protected long getCurrentUserId() {
        String token = request.getHeader(appProperties.getLoginHeader());
        return tokenService.verify(token);
    }
}
