package com.qhr.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.service.DmnDecisionService;
import com.qhr.vo.Person;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class DmnDecisionServiceImpl implements DmnDecisionService {

  private static final String DMN_RESOURCE = "/dmn/TestDmn.dmn";
  private static final String INPUT_NAME = "Company";
  private static final String DECISION_NAME = "common_precheck";
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };

  private final ObjectMapper objectMapper;
  private DmnDecisionModel decisionModel;

  /**
   * 注入 JSON 转换工具，供请求对象到 DMN 输入上下文的映射使用。
   */
  public DmnDecisionServiceImpl(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * 在 Bean 初始化完成后预加载 DMN 模型，避免每次请求重复解析规则文件。
   */
  @PostConstruct
  void init() {
    decisionModel = loadDecisionModel();
  }

  /**
   * 执行预校验 DMN 决策，返回指定决策节点的结果。
   */
  @Override
  public Object evaluate(Person request) {
    Map<String, Object> input = Map.of(INPUT_NAME, buildCompanyInput(request));
    DMNResult result = decisionModel.evaluateAll(decisionModel.newContext(input));
    assertEvaluationSucceeded(result);
    return extractDecisionResult(result);
  }

  /**
   * 从 classpath 读取 DMN 文件并构建可复用的 Kogito 决策模型。
   */
  private DmnDecisionModel loadDecisionModel() {
    try (InputStream inputStream = getClass().getResourceAsStream(DMN_RESOURCE)) {
      if (inputStream == null) {
        throw new ApiException(ApiCode.NOT_FOUND, "DMN文件不存在: " + DMN_RESOURCE);
      }
      try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
        var runtime = DMNKogito.createGenericDMNRuntime(Set.of(), false, reader);
        var model = runtime.getModels().stream()
            .findFirst()
            .orElseThrow(() -> new ApiException(ApiCode.NOT_FOUND, "DMN模型未找到: " + DMN_RESOURCE));
        return new DmnDecisionModel(runtime, model.getNamespace(), model.getName());
      }
    } catch (IOException exception) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "读取DMN文件失败: " + exception.getMessage());
    }
  }

  /**
   * 将请求对象转换为 DMN 输入数据结构中的 Company 上下文。
   */
  private Map<String, Object> buildCompanyInput(Person request) {
    return new LinkedHashMap<>(objectMapper.convertValue(request, MAP_TYPE));
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

  /**
   * 提取目标决策节点的执行结果，若节点不存在则抛出服务端异常。
   */
  private Object extractDecisionResult(DMNResult result) {
    DMNDecisionResult decisionResult = result.getDecisionResultByName(DECISION_NAME);
    if (decisionResult == null) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "DMN决策不存在: " + DECISION_NAME);
    }
    return decisionResult.getResult();
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
