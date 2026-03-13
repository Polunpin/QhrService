package com.qhr.controller;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.service.DmnDecisionService;
import com.qhr.vo.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@ApplicationScoped
@Path("/api/dmn")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DmnDecisionController {

  private final DmnDecisionService dmnDecisionService;

  public DmnDecisionController(DmnDecisionService dmnDecisionService) {
    this.dmnDecisionService = dmnDecisionService;
  }

  /**
   * 按DMN决策链执行产品匹配。
   */
  @POST
  @Path("/evaluate")
  public ApiResponse evaluate(Person request) {
    ApiAssert.notNull(request, ApiCode.BAD_REQUEST, "请求体不能为空");
    return ApiResponse.ok(dmnDecisionService.evaluate(request));
  }
}
