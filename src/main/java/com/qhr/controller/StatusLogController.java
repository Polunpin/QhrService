package com.qhr.controller;

import com.qhr.config.*;
import com.qhr.model.StatusLog;
import com.qhr.service.StatusLogService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/*操作日志*/
@ApplicationScoped
@Path("/api/status-logs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StatusLogController {

  private final StatusLogService statusLogService;
  public StatusLogController(StatusLogService statusLogService) {
    this.statusLogService = statusLogService;
  }

  /** 分页查询订单日志 */
  @GET
  @Path("/order/{orderId}")
  public ApiResponse listByOrder(@PathParam("orderId") Long orderId,
                                 @QueryParam("page") Integer page,
                                 @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<StatusLog> logs = statusLogService.listByOrderId(orderId, bounds.offset(), bounds.size());
    long total = statusLogService.countByOrderId(orderId);
    return ApiResponse.ok(PageResult.of(logs, total, bounds.page(), bounds.size()));
  }

  /** 创建日志 */
  @POST
  public ApiResponse create(StatusLog log) {
    ApiAssert.notNull(log, ApiCode.BAD_REQUEST, "请求体log不能为空");
    return ApiResponse.ok(statusLogService.create(log));
  }

  /**
   * 查询日志详情
   */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(statusLogService.getById(id));
  }
}
