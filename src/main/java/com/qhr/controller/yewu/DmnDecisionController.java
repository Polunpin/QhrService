package com.qhr.controller.yewu;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.dto.EnterprisePayload;
import com.qhr.service.DmnDecisionService;
import com.qhr.vo.ApplicantProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/*DMN客户端*/
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
   * 预审
   * 1.注册日期：大于1年
   * 2.状态：在业、存续
   */
  @Deprecated
  @POST
  @Path("/precheck")
  public ApiResponse precheck(EnterprisePayload request) {
    ApiAssert.notNull(request, ApiCode.BAD_REQUEST, "请求体不能为空");
    return ApiResponse.ok(dmnDecisionService.precheck(request));
  }
  /**
   * 产品匹配
   * industry：行业
   * abnormal：异常
   * illegal：非法
   * judicialRisk：司法风险
   */
  @POST
  @Path("/match")
  public ApiResponse match(ApplicantProfile applicantProfile) {
    ApiAssert.notNull(applicantProfile, ApiCode.BAD_REQUEST, "请求体不能为空");
    return ApiResponse.ok(dmnDecisionService.match(applicantProfile));
  }
}
