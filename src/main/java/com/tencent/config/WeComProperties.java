package com.tencent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "we-com")
public record WeComProperties(String corpId,
                              String agentId,
                              String corpSecret,
                              String redirectUri) {
}
