package com.tencent.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "we-com")
public interface WeComProperties {
  String corpId();
  String agentId();
  String corpSecret();
  String redirectUri();
}
