package com.tencent.controller;

import com.tencent.config.*;
import com.tencent.dto.AdvanceStageRequest;
import com.tencent.dto.AssignStaffRequest;
import com.tencent.dto.UpdateSettleStatusRequest;
import com.tencent.dto.UpdateStringStatusRequest;
import com.tencent.model.CustomServiceOrder;
import com.tencent.service.CustomServiceOrderService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path("/api/custom-service-orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomServiceOrderController {

  private final CustomServiceOrderService orderService;
  public CustomServiceOrderController(CustomServiceOrderService orderService) {
    this.orderService = orderService;
  }

  /** 查询订单详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    CustomServiceOrder order = orderService.getById(id);
    ApiAssert.notNull(order, ApiCode.NOT_FOUND, "订单不存在");
    return ApiResponse.ok(order);
  }

  /** 分页查询订单列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("enterpriseId") Long enterpriseId,
                          @QueryParam("staffId") Long staffId,
                          @QueryParam("serviceStatus") String serviceStatus,
                          @QueryParam("currentStage") String currentStage,
                          @QueryParam("settleStatus") String settleStatus,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<CustomServiceOrder> orders = orderService.list(enterpriseId, staffId, serviceStatus, currentStage, settleStatus,
        bounds.offset(), bounds.size());
    long total = orderService.count(enterpriseId, staffId, serviceStatus, currentStage, settleStatus);
    return ApiResponse.ok(PageResult.of(orders, total, bounds.page(), bounds.size()));
  }

  /** 创建订单 */
  @POST
  public ApiResponse create(CustomServiceOrder order) {
    Long id = orderService.create(order);
    return ApiResponse.ok(id);
  }

  /** 更新订单 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, CustomServiceOrder order) {
    ApiAssert.isTrue(orderService.update(order.withId(id)), ApiCode.NOT_FOUND, "订单不存在");
    return ApiResponse.ok(true);
  }

  /** 删除订单 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(orderService.delete(id), ApiCode.NOT_FOUND, "订单不存在");
    return ApiResponse.ok(true);
  }

  /** 指派订单负责人 */
  @POST
  @Path("/{id}/assign")
  public ApiResponse assignStaff(@PathParam("id") Long id, AssignStaffRequest request) {
    ApiAssert.isTrue(orderService.assignStaff(id, request.staffId()), ApiCode.NOT_FOUND, "订单不存在");
    return ApiResponse.ok(true);
  }

  /** 更新订单服务状态 */
  @POST
  @Path("/{id}/service-status")
  public ApiResponse updateServiceStatus(@PathParam("id") Long id,
                                         UpdateStringStatusRequest request) {
    ApiAssert.isTrue(orderService.updateServiceStatus(id, request.status()), ApiCode.NOT_FOUND, "订单不存在");
    return ApiResponse.ok(true);
  }

  /** 更新订单结算状态 */
  @POST
  @Path("/{id}/settle-status")
  public ApiResponse updateSettleStatus(@PathParam("id") Long id,
                                        UpdateSettleStatusRequest request) {
    ApiAssert.isTrue(orderService.updateSettleStatus(id, request.settleStatus()),
        ApiCode.NOT_FOUND, "订单不存在");
    return ApiResponse.ok(true);
  }

  /** 推进订单阶段并写入日志 */
  @POST
  @Path("/{id}/stage")
  public ApiResponse advanceStage(@PathParam("id") Long id, AdvanceStageRequest request) {
    boolean ok = orderService.advanceStage(id, request.postStage(), request.serviceStatus(),
        request.remark(), request.operatorType(), request.operatorId());
    ApiAssert.isTrue(ok, ApiCode.NOT_FOUND, "订单不存在");
    return ApiResponse.ok(true);
  }
}
