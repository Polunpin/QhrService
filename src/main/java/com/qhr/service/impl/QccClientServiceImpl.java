package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.qcc.QccAuthHeaders;
import com.qhr.config.qcc.QccAuthSigner;
import com.qhr.config.qcc.QccClientException;
import com.qhr.service.QccClientService;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ApplicationScoped
public class QccClientServiceImpl implements QccClientService {

  @ConfigProperty(name = "qcc-key")
  String appKey;
  @ConfigProperty(name = "qcc-secretKey")
  String secretKey;

  //企业模糊查询
  private static final String BASE_URL = "https://api.qichacha.com/FuzzySearch/GetList";

  private final QccAuthSigner qccAuthSigner;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public QccClientServiceImpl(QccAuthSigner qccAuthSigner, ObjectMapper objectMapper) {
    this.qccAuthSigner = qccAuthSigner;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
  }

  @Override
  public JsonNode getList(String searchKey) {
    if (searchKey == null || searchKey.isBlank()) {
      throw new IllegalArgumentException("searchKey不能为空");
    }

    QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
    HttpRequest request = HttpRequest.newBuilder(buildUri(appKey, searchKey))
        .GET()
        .timeout(Duration.ofSeconds(10))
        .header("Token", authHeaders.token())
        .header("Timespan", authHeaders.timespan())
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (IOException exception) {
      throw new QccClientException("调用企查查企业模糊搜索接口失败", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new QccClientException("调用企查查企业模糊搜索接口被中断", exception);
    }

    if (response.statusCode() != 200) {
      throw new QccClientException("调用企查查企业模糊搜索接口失败，HTTP状态码=" + response.statusCode());
    }

    try {
      JsonNode root = objectMapper.readTree(response.body());
      return root.path("Result");
    } catch (IOException exception) {
      throw new QccClientException("解析企查查企业模糊搜索响应失败", exception);
    }
  }

  //get 请求路径拼接
  private URI buildUri(String appKey, String searchKey) {
    String builder = BASE_URL + "?key=" + encode(appKey) + "&searchKey=" + encode(searchKey);
    return URI.create(builder);
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
