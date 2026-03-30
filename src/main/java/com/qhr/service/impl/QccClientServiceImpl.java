package com.qhr.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.qcc.QccAuthHeaders;
import com.qhr.config.qcc.QccAuthSigner;
import com.qhr.config.qcc.QccClientException;
import com.qhr.dto.QccTaxCreateOrderRequest;
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
  private static final String FUZZY_SEARCH_URL = "https://api.qichacha.com/FuzzySearch/GetList";
  //企业财税数据 第一步：创建订单
  private static final String TAX_CREATE_ORDER_URL = "https://api.qichacha.com/TaxData/CreateOrder";
  //企业财税数据 第二步：验证码发送
  private static final String TAX_SEND_CODE_URL = "https://api.qichacha.com/TaxData/SendCode";
  //企业财税数据 第三步：数据获取
  private static final String TAX_GET_DATA_URL = "https://api.qichacha.com/TaxData/GetData";

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

    QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
    HttpRequest request = HttpRequest.newBuilder(buildUri(
                    FUZZY_SEARCH_URL,
                    "key", appKey,
                    "searchKey", searchKey
            ))
        .GET()
        .timeout(Duration.ofSeconds(10))
        .header("Token", authHeaders.token())
        .header("Timespan", authHeaders.timespan())
        .build();

    //请求
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

    //解析
    try {
      JsonNode root = objectMapper.readTree(response.body());
      return root.path("Result");
    } catch (IOException exception) {
      throw new QccClientException("解析企查查企业模糊搜索响应失败", exception);
    }
  }

  @Override
  public JsonNode taxData(QccTaxCreateOrderRequest requestBody) {
    validateTaxDataRequest(requestBody);

    //下单
    JsonNode createOrderResult = createTaxOrder(requestBody);
    //验证码发送
    String orderNo = extractRequiredText(createOrderResult, "OrderNo", "企查查财税下单响应缺少OrderNo");

    sendCode(orderNo, requestBody.verifyCode());
    return getData(orderNo);
  }

  private URI buildUri(String baseUrl, String... queryPairs) {
    if (queryPairs.length % 2 != 0) {
      throw new IllegalArgumentException("queryPairs必须是成对的key/value");
    }
    StringBuilder builder = new StringBuilder(baseUrl);
    boolean hasQuery = false;
    for (int i = 0; i < queryPairs.length; i += 2) {
      String key = queryPairs[i];
      String value = queryPairs[i + 1];
      if (value == null || value.isBlank()) {
        continue;
      }
      builder.append(hasQuery ? '&' : '?')
              .append(encode(key))
              .append('=')
              .append(encode(value));
      hasQuery = true;
    }
    return URI.create(builder.toString());
  }

  private HttpRequest.BodyPublisher buildJsonBody(Object requestBody) {
    try {
      return HttpRequest.BodyPublishers.ofString(
              objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8
      );
    } catch (IOException exception) {
      throw new QccClientException("序列化企查查请求失败", exception);
    }
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private JsonNode createTaxOrder(QccTaxCreateOrderRequest requestBody) {
    QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
    HttpRequest request = HttpRequest.newBuilder(buildUri(
                    TAX_CREATE_ORDER_URL,
                    "key", appKey
            ))
            .POST(buildJsonBody(requestBody))
            .timeout(Duration.ofSeconds(10))
            .header("Token", authHeaders.token())
            .header("Timespan", authHeaders.timespan())
            .header("Content-Type", "application/json")
            .build();
    return execute(request, "调用企查查财税下单接口");
  }

  private JsonNode sendCode(String orderNo, String verifyCode) {
    QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
    HttpRequest request = HttpRequest.newBuilder(buildUri(
                    TAX_SEND_CODE_URL,
                    "key", appKey,
                    "orderNo", orderNo,
                    "verifyCode", verifyCode
            ))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .header("Token", authHeaders.token())
            .header("Timespan", authHeaders.timespan())
            .build();
    return execute(request, "调用企查查财税验证码发送接口");
  }

  private JsonNode getData(String orderNo) {
    QccAuthHeaders authHeaders = qccAuthSigner.sign(appKey, secretKey);
    HttpRequest request = HttpRequest.newBuilder(buildUri(
                    TAX_GET_DATA_URL,
                    "key", appKey,
                    "orderNo", orderNo
            ))
            .GET()
            .timeout(Duration.ofSeconds(10))
            .header("Token", authHeaders.token())
            .header("Timespan", authHeaders.timespan())
            .build();
    return execute(request, "调用企查查财税数据获取接口");
  }

  private JsonNode execute(HttpRequest request, String action) {
    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    } catch (IOException exception) {
      throw new QccClientException(action + "失败", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new QccClientException(action + "被中断", exception);
    }

    if (response.statusCode() != 200) {
      throw new QccClientException(action + "失败，HTTP状态码=" + response.statusCode());
    }

    try {
      return objectMapper.readTree(response.body());
    } catch (IOException exception) {
      throw new QccClientException(action + "响应解析失败", exception);
    }
  }

  private void validateTaxDataRequest(QccTaxCreateOrderRequest requestBody) {
    if (requestBody == null) {
      throw new IllegalArgumentException("企查查财税数据请求不能为空");
    }
    if (requestBody.searchKey() == null || requestBody.searchKey().isBlank()) {
      throw new IllegalArgumentException("searchKey不能为空");
    }
    if (requestBody.userName() == null || requestBody.userName().isBlank()) {
      throw new IllegalArgumentException("userName不能为空");
    }
    if (requestBody.password() == null || requestBody.password().isBlank()) {
      throw new IllegalArgumentException("password不能为空");
    }
    if (requestBody.verifyCode() == null || requestBody.verifyCode().isBlank()) {
      throw new IllegalArgumentException("verifyCode不能为空");
    }
  }

  private String extractRequiredText(JsonNode root, String fieldName, String errorMessage) {
    JsonNode value = root.path("Result").path(fieldName);
    if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
      throw new QccClientException(errorMessage);
    }
    return value.asText();
  }
}
