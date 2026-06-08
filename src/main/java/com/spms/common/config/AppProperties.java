package com.spms.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String projectName = "spms";
    private String loginHeader = "Authorization";
    private String accessTokenSecret = "1234567890";
    private long loginTokenExpireSecond = 86400;
}
