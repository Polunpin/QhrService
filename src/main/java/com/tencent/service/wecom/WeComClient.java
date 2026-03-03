package com.tencent.service.wecom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.config.ApiCode;
import com.tencent.config.ApiException;
import com.tencent.config.WeComProperties;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@ApplicationScoped
public class WeComClient {

  private static final String BASE_URL = "https://qyapi.weixin.qq.com";
  private static final long TOKEN_EARLY_EXPIRE_SECONDS = 60;

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final WeComProperties properties;

  private volatile String cachedToken;
  private volatile Instant tokenExpiresAt;
  public WeComClient(WeComProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
  }
  public WeComAuthResponse getAuthUser(String code) {
    String token = getAccessToken();
    WeComAuthResponse response = get(
        "/cgi-bin/auth/getuserinfo?access_token=" + encode(token) + "&code=" + encode(code),
        WeComAuthResponse.class);
    ensureSuccess(response);
    return response;
  }
  public WeComUserDetailResponse getUserDetail(String userId) {
    String token = getAccessToken();
    WeComUserDetailResponse response = get(
        "/cgi-bin/user/get?access_token=" + encode(token) + "&userid=" + encode(userId),
        WeComUserDetailResponse.class);
    ensureSuccess(response);
    return response;
  }

  private String getAccessToken() {
    Instant now = Instant.now();
    if (cachedToken != null && tokenExpiresAt != null && now.isBefore(tokenExpiresAt)) {
      return cachedToken;
    }
    synchronized (this) {
      if (cachedToken != null && tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt)) {
        return cachedToken;
      }
      String corpId = requireValue(properties.corpId(), "企业微信 corpId");
      String corpSecret = requireValue(properties.corpSecret(), "企业微信 corpSecret");
      WeComTokenResponse response = get(
          "/cgi-bin/gettoken?corpid=" + encode(corpId) + "&corpsecret=" + encode(corpSecret),
          WeComTokenResponse.class);
      ensureSuccess(response);
      if (response.accessToken() == null || response.expiresIn() == null) {
        throw new ApiException(ApiCode.INTERNAL_ERROR, "企业微信 access_token 响应异常");
      }
      cachedToken = response.accessToken();
      long expiresIn = Math.max(0, response.expiresIn() - TOKEN_EARLY_EXPIRE_SECONDS);
      tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
      return cachedToken;
    }
  }

  private <T extends WeComBaseResponse> T get(String pathWithQuery, Class<T> responseType) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(BASE_URL + pathWithQuery))
        .GET()
        .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new ApiException(ApiCode.INTERNAL_ERROR, "企业微信接口调用失败: HTTP " + response.statusCode());
      }
      return objectMapper.readValue(response.body(), responseType);
    } catch (IOException e) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "企业微信响应解析失败: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(ApiCode.INTERNAL_ERROR, "企业微信接口调用被中断");
    }
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static String requireValue(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new ApiException(ApiCode.BAD_REQUEST, message);
    }
    return value;
  }

  private static void ensureSuccess(WeComBaseResponse response) {
    if (response == null) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "企业微信响应为空");
    }
    if (response.errCode() != null && response.errCode() != 0) {
      String msg = response.errMsg() == null ? "企业微信调用失败" : response.errMsg();
      throw new ApiException(ApiCode.BAD_REQUEST, msg + " (errCode=" + response.errCode() + ")");
    }
  }
  public interface WeComBaseResponse {
    Integer errCode();

    String errMsg();
  }
  public record WeComTokenResponse(Integer errCode,
                                   String errMsg,
                                   @JsonProperty("access_token") String accessToken,
                                   @JsonProperty("expires_in") Integer expiresIn) implements WeComBaseResponse {
  }
  public record WeComAuthResponse(Integer errCode,
                                  String errMsg,
                                  String userid,
                                  @JsonProperty("user_ticket") String userTicket,
                                  String openid) implements WeComBaseResponse {
  }
  public record WeComUserDetailResponse(Integer errCode,
                                        String errMsg,
                                        String userid,
                                        String name,
                                        String mobile,
                                        String unionid,
                                        String email) implements WeComBaseResponse {
  }
}
