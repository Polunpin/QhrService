package com.qhr.controller.yewu;

import com.qhr.config.ApiResponse;
import com.qhr.dto.EnterprisePayload;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.service.MeasureProgressService;
import com.qhr.service.MeasureService;
import com.qhr.vo.MeasureProgressVO;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestStreamElementType;

/**
 * 小程序测额入口。
 * 提供预审、提交以及任务进度查询/订阅能力。
 */
@ApplicationScoped
@Path("/api/measure")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MeasureController {

  private final MeasureService measureService;
  private final MeasureProgressService measureProgressService;

  public MeasureController(MeasureService measureService, MeasureProgressService measureProgressService) {
    this.measureService = measureService;
    this.measureProgressService = measureProgressService;
  }

  /**
   * 企业基础信息预审。
   */
  @POST
  @Path("/precheck")
  public ApiResponse precheck(EnterprisePayload request, @Context HttpHeaders headers) {
    return ApiResponse.ok(measureService.precheck(request, headers.getHeaderString("x-wx-openid"),
            headers.getHeaderString("x-wx-unionid")));
  }

  /**
   * 提交测额任务并立即返回任务信息。
   */
  @POST
  @Path("/submit")
  public ApiResponse submit(MeasureSubmitRequest request, @Context HttpHeaders headers) {
    return ApiResponse.ok(measureService.submit(request, headers.getHeaderString("x-wx-openid")));
  }

  /**
   * 查询指定融资需求的进度快照。
   */
  @GET
  @Path("/progress/{intentionId}")
  public ApiResponse progress(@PathParam("intentionId") Long intentionId, @Context HttpHeaders headers) {
    return ApiResponse.ok(measureProgressService.getProgress(intentionId, headers.getHeaderString("x-wx-openid")));
  }

  /**
   * 以 SSE 方式持续推送进度变更。
   */
  @GET
  @Path("/progress/{intentionId}/stream")
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  public Multi<MeasureProgressVO> stream(@PathParam("intentionId") Long intentionId,
                                         @Context HttpHeaders headers) {
    return measureProgressService.stream(intentionId, headers.getHeaderString("x-wx-openid"));
  }
}
