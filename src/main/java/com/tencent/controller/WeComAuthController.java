package com.tencent.controller;

import com.tencent.config.ApiCode;
import com.tencent.config.ApiException;
import com.tencent.config.ApiResponse;
import com.tencent.config.WeComProperties;
import com.tencent.service.wecom.WeComAuthService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ApplicationScoped
@Path("/api/auth/wecom")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WeComAuthController {

  private final WeComAuthService authService;
  private final WeComProperties properties;

  public WeComAuthController(WeComAuthService authService, WeComProperties properties) {
    this.authService = authService;
    this.properties = properties;
  }

  @GET
  @Path("/login-url")
  public ApiResponse loginUrl(@QueryParam("redirectUri") String redirectUri,
                              @QueryParam("state") String state) {
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

  @GET
  @Path("/callback")
  public ApiResponse callback(@QueryParam("code") String code,
                              @QueryParam("state") String state) {
    return ApiResponse.ok(authService.loginByCode(code));
  }

  private static String requireValue(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new ApiException(ApiCode.BAD_REQUEST, message);
    }
    return value;
  }
}
