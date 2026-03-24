package com.qhr.controller.yewu;

import com.qhr.config.ApiResponse;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.service.MeasureService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/*测额提交*/
@ApplicationScoped
@Path("/api/measure")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MeasureController {

  private final MeasureService measureService;

  public MeasureController(MeasureService measureService) {
    this.measureService = measureService;
  }

  @POST
  @Path("/submit")
  public ApiResponse submit(MeasureSubmitRequest request,@Context HttpHeaders headers) {
    //TODO 本地测试
    return ApiResponse.ok(measureService.submit(request, "om70g7YsunOZY-hhhSw2mli1aQKg",
            "oXiKu6IQxx5OvraUpd5Mp_x5Ecd0"));
    //部署版本
//    return ApiResponse.ok(measureService.submit(request, headers.getHeaderString("x-wx-openid"),
//            headers.getHeaderString("x-wx-unionid")));
  }
}