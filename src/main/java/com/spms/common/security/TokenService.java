package com.spms.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.spms.common.config.AppProperties;
import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import org.springframework.stereotype.Service;

import java.util.OptionalLong;

@Service
public class TokenService {
    private final AppProperties appProperties;

    public TokenService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public long verify(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(appProperties.getAccessTokenSecret())).build();
            return Long.parseLong(verifier.verify(token).getSubject());
        } catch (Exception exception) {
            throw new AppException(CommonError.UNAUTHORIZED, "无效的令牌");
        }
    }

    public OptionalLong tryVerify(String token) {
        if (token == null || token.isBlank()) {
            return OptionalLong.empty();
        }
        try {
            return OptionalLong.of(verify(token.trim()));
        } catch (AppException exception) {
            if (exception.getResultCode() == CommonError.UNAUTHORIZED) {
                return OptionalLong.empty();
            }
            throw exception;
        }
    }

    public String create(long userId) {
        return JWT.create()
                .withSubject(String.valueOf(userId))
                .sign(Algorithm.HMAC256(appProperties.getAccessTokenSecret()));
    }
}
