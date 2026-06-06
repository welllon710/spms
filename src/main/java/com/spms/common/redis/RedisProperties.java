package com.spms.common.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.redis")
public class RedisProperties {
    private String prefix = "";
    private long cacheExpireSecond = 86400;
    private long lockTimeoutMillis = 3000;
}
