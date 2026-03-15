package com.qhr.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.service.DmnDecisionService;
import com.qhr.vo.Person;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kie.dmn.api.core.DMNResult;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.dmn.DmnDecisionModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DmnDecisionServiceImpl implements DmnDecisionService {

  private static final String DMN_RESOURCE = "/dmn/TestDmn.dmn";

  private DmnDecisionModel decisionModel;
  @Inject
  ObjectMapper objectMapper;

  @PostConstruct
  void init() {
    try (InputStream inputStream = getClass().getResourceAsStream(DMN_RESOURCE)) {
      if (inputStream == null) {
        throw new ApiException(ApiCode.NOT_FOUND, "DMN文件不存在: " + DMN_RESOURCE);
      }

      try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
        var runtime = DMNKogito.createGenericDMNRuntime(Set.of(), false, reader);
        var model = runtime.getModels().stream().findFirst()
            .orElseThrow(() -> new ApiException(ApiCode.NOT_FOUND, "DMN模型未找到: " + DMN_RESOURCE));
        decisionModel = new DmnDecisionModel(runtime, model.getNamespace(), model.getName());
      }
    } catch (IOException exception) {
      throw new ApiException(ApiCode.INTERNAL_ERROR, "读取DMN文件失败: " + exception.getMessage());
    }
  }

  @Override
  public Object evaluate(Person request) {
    Map<String, Object> input = objectMapper.convertValue(request, new TypeReference<>() {});

    DMNResult result = decisionModel.evaluateAll(decisionModel.newContext(input));
    if (result.hasErrors()) {
      String message = result.getMessages().stream()
              .map(dmnMessage -> dmnMessage.getLevel() + ": " + dmnMessage.getText())
              .reduce((left, right) -> left + "; " + right)
              .orElse("DMN evaluation failed");
      throw new ApiException(ApiCode.BAD_REQUEST, message);
    }

    return result.getDecisionResultByName("common_precheck").getResult();
  }
}
