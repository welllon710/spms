package com.spms.common.security;

import com.spms.common.config.AppProperties;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.redis.RedisHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LoginSessionService {
    private static final String LOGIN_TOKEN_KEY_PREFIX = "login_token_";

    private final RedisHelper redisHelper;
    private final AppProperties appProperties;

    public void save(long userId, String token) {
        redisHelper.set(getLoginTokenKey(userId), token, appProperties.getLoginTokenExpireSecond());
    }

    public void verify(long userId, String token) {
        String cachedToken = redisHelper.get(getLoginTokenKey(userId));
        if (!Objects.equals(cachedToken, token)) {
            throw new AppException(CommonError.UNAUTHORIZED, "登录已过期，请重新登录");
        }
    }

    public void delete(long userId) {
        redisHelper.delete(getLoginTokenKey(userId));
    }

    private String getLoginTokenKey(long userId) {
        return LOGIN_TOKEN_KEY_PREFIX + userId;
    }
}
