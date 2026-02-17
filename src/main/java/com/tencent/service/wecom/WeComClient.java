package com.tencent.service.wecom;

import com.tencent.config.ApiCode;
import com.tencent.config.ApiException;
import com.tencent.config.WeComProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Component
public class WeComClient {

  private static final String BASE_URL = "https://qyapi.weixin.qq.com";
  private static final long TOKEN_EARLY_EXPIRE_SECONDS = 60;

  private final RestClient restClient;
  private final WeComProperties properties;

  private volatile String cachedToken;
  private volatile Instant tokenExpiresAt;

  public WeComClient(WeComProperties properties) {
    this.properties = properties;
    this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
  }

  public WeComAuthResponse getAuthUser(String code) {
    String token = getAccessToken();
    WeComAuthResponse response = restClient.get()
        .uri("/cgi-bin/auth/getuserinfo?access_token={token}&code={code}", token, code)
        .retrieve()
        .body(WeComAuthResponse.class);
    ensureSuccess(response);
    return response;
  }

  public WeComUserDetailResponse getUserDetail(String userId) {
    String token = getAccessToken();
    WeComUserDetailResponse response = restClient.get()
        .uri("/cgi-bin/user/get?access_token={token}&userid={userId}", token, userId)
        .retrieve()
        .body(WeComUserDetailResponse.class);
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
      WeComTokenResponse response = restClient.get()
          .uri("/cgi-bin/gettoken?corpid={corpId}&corpsecret={corpSecret}", corpId, corpSecret)
          .retrieve()
          .body(WeComTokenResponse.class);
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
