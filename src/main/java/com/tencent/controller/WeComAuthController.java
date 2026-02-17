package com.tencent.controller;

import com.tencent.config.ApiCode;
import com.tencent.config.ApiException;
import com.tencent.config.ApiResponse;
import com.tencent.config.WeComProperties;
import com.tencent.service.wecom.WeComAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/wecom")
public class WeComAuthController {

  private final WeComAuthService authService;
  private final WeComProperties properties;

  public WeComAuthController(WeComAuthService authService, WeComProperties properties) {
    this.authService = authService;
    this.properties = properties;
  }

  @GetMapping("/login-url")
  public ApiResponse loginUrl(@RequestParam(required = false) String redirectUri,
                              @RequestParam(required = false) String state) {
    String corpId = requireValue(properties.corpId(), "企业微信 corpId");
    String agentId = requireValue(properties.agentId(), "企业微信 agentId");
    String finalRedirectUri = redirectUri == null || redirectUri.isBlank()
        ? requireValue(properties.redirectUri(), "企业微信 redirectUri")
        : redirectUri;
    String finalState = state == null || state.isBlank() ? "wecom_login" : state;
    String encodedRedirect = URLEncoder.encode(finalRedirectUri, StandardCharsets.UTF_8);
    String url = "https://open.work.weixin.qq.com/wwopen/sso/qrConnect?appid=" + corpId
        + "&agentid=" + agentId
        + "&redirect_uri=" + encodedRedirect
        + "&state=" + finalState;
    return ApiResponse.ok(Map.of(
        "url", url,
        "state", finalState,
        "redirectUri", finalRedirectUri
    ));
  }

  @GetMapping("/callback")
  public ApiResponse callback(@RequestParam String code,
                              @RequestParam(required = false) String state) {
    return ApiResponse.ok(authService.loginByCode(code));
  }

  private static String requireValue(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new ApiException(ApiCode.BAD_REQUEST, message);
    }
    return value;
  }
}
