package com.qhr.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.model.ProductRule;
import com.qhr.service.DmnDecisionService;
import com.qhr.service.ProductRuleService;
import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.Person;
import com.qhr.vo.PrecheckResult;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNResult;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.dmn.DmnDecisionModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class DmnDecisionServiceImpl implements DmnDecisionService {

  private static final String PRECHECK_RESOURCE = "/dmn/common_precheck.dmn";
  private static final String PRECHECK_DECISION_NAME = "common_precheck";
  private static final String PRODUCT_MATCH_RESOURCE = "/dmn/Rating and Matching.dmn";
  private static final String PRODUCT_MATCH_DECISION_NAME = "ProductFilter";
  private static final String COMPANY_INPUT_NAME = "Company";
  private static final String APPLICANT_PROFILE_INPUT_NAME = "ApplicantProfile";
  private static final String PRODUCT_RULE_INPUT_NAME = "ProductRule";
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };
  private static final TypeReference<List<ProductRule>> PRODUCT_RULE_LIST_TYPE = new TypeReference<>() {
  };

  private final ObjectMapper objectMapper;
  private final ProductRuleService productRuleService;
  private final Map<String, DmnDecisionModel> decisionModels = new HashMap<>();

  /**
   * 注入 JSON 转换工具，供请求对象到 DMN 输入上下文的映射使用。
   */
  public DmnDecisionServiceImpl(ObjectMapper objectMapper, ProductRuleService productRuleService) {
    this.objectMapper = objectMapper;
    this.productRuleService = productRuleService;
  }

  /**
   * 在 Bean 初始化完成后预加载 DMN 模型，避免每次请求重复解析规则文件。
   */
  @PostConstruct
  void init() {
    decisionModels.put(PRECHECK_RESOURCE, loadDecisionModel(PRECHECK_RESOURCE));
    decisionModels.put(PRODUCT_MATCH_RESOURCE, loadDecisionModel(PRODUCT_MATCH_RESOURCE));
  }

  /**
   * 执行预校验 DMN 决策，返回预审结果。
   */
  @Override
  public Object precheck(Person request) {
    return evaluateDecision(PRECHECK_RESOURCE, PRECHECK_DECISION_NAME,
            Map.of(COMPANY_INPUT_NAME, buildDmnInput(request)), PrecheckResult.class);
  }

  /**
   * 产品匹配+信用评分
   */
  @Override
  public Object match(ApplicantProfile applicantProfile) {
    List<ProductRule> items = productRuleService.list();
    Map<String, Object> input = Map.of(
            APPLICANT_PROFILE_INPUT_NAME, buildDmnInput(applicantProfile),
            PRODUCT_RULE_INPUT_NAME, items.stream().map(this::buildDmnInput).toList());
    return evaluateDecision(PRODUCT_MATCH_RESOURCE, PRODUCT_MATCH_DECISION_NAME, input,
            PRODUCT_RULE_LIST_TYPE);
  }

  /**
   * 从 classpath 读取 DMN 文件并构建可复用的 Kogito 决策模型。
   */
  private DmnDecisionModel loadDecisionModel(String resource) {
    try (InputStream inputStream = getClass().getResourceAsStream(resource)) {
      if (inputStream == null) {
        throw new ApiException(ApiCode.NOT_FOUND, "DMN文件不存在: " + resource);
      }
      try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
        var runtime = DMNKogito.createGenericDMNRuntime(Set.of(), false, reader);
        var model = runtime.getModels().stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(ApiCode.NOT_FOUND, "DMN模型未找到: " + resource));
        return new DmnDecisionModel(runtime, model.getNamespace(), model.getName());
      }
    } catch (IOException exception) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "读取DMN文件失败: " + exception.getMessage());
    }
  }

  /**
   * 将请求对象转换为 DMN 输入数据结构中的 Company 上下文。
   */
  private Map<String, Object> buildDmnInput(Object request) {
    return new LinkedHashMap<>(objectMapper.convertValue(request, MAP_TYPE));
  }

  private DmnDecisionModel getDecisionModel(String resource) {
    DmnDecisionModel model = decisionModels.get(resource);
    if (model != null) {
      return model;
    }
    DmnDecisionModel loadedModel = loadDecisionModel(resource);
    decisionModels.put(resource, loadedModel);
    return loadedModel;
  }

  private <T> T evaluateDecision(String resource,
                                 String decisionName,
                                 Map<String, Object> input,
                                 Class<T> type) {
    DMNResult result = evaluateDecision(resource, input);
    return extractDecisionResult(result, decisionName, type);
  }

  private <T> T evaluateDecision(String resource,
                                 String decisionName,
                                 Map<String, Object> input,
                                 TypeReference<T> type) {
    DMNResult result = evaluateDecision(resource, input);
    return extractDecisionResult(result, decisionName, type);
  }

  private DMNResult evaluateDecision(String resource, Map<String, Object> input) {
    DmnDecisionModel decisionModel = getDecisionModel(resource);
    DMNResult result = decisionModel.evaluateAll(decisionModel.newContext(input));
    assertEvaluationSucceeded(result);
    return result;
  }

  /**
   * 校验 DMN 执行结果是否成功；若存在错误则统一抛出业务异常。
   */
  private void assertEvaluationSucceeded(DMNResult result) {
    if (!result.hasErrors()) {
      return;
    }
    throw new ApiException(ApiCode.BAD_REQUEST, formatMessages(result.getMessages()));
  }

  private <T> T extractDecisionResult(DMNResult result, String decisionName, Class<T> type) {
    DMNDecisionResult decisionResult = result.getDecisionResultByName(decisionName);
    if (decisionResult == null) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "DMN决策不存在: " + decisionName);
    }
    return objectMapper.convertValue(decisionResult.getResult(), type);
  }

  private <T> T extractDecisionResult(DMNResult result, String decisionName, TypeReference<T> type) {
    DMNDecisionResult decisionResult = result.getDecisionResultByName(decisionName);
    if (decisionResult == null) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "DMN决策不存在: " + decisionName);
    }
    return objectMapper.convertValue(decisionResult.getResult(), type);
  }

  /**
   * 将 DMN 引擎返回的消息列表拼接成单条可读错误信息。
   */
  private String formatMessages(List<DMNMessage> messages) {
    String message = messages.stream()
            .map(dmnMessage -> dmnMessage.getLevel() + ": " + dmnMessage.getText())
            .collect(Collectors.joining("; "));
    if (!message.isBlank()) {
      return message;
    }
    return "DMN evaluation failed";
  }
}
