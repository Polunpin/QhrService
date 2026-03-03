package com.tencent.controller;

import com.tencent.config.*;
import com.tencent.dto.UpdateIntentionStatusRequest;
import com.tencent.model.FinancingIntention;
import com.tencent.service.FinancingIntentionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path("/api/financing-intentions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FinancingIntentionController {

  private final FinancingIntentionService intentionService;
  public FinancingIntentionController(FinancingIntentionService intentionService) {
    this.intentionService = intentionService;
  }

  /** 查询融资意向详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    FinancingIntention intention = intentionService.getById(id);
    ApiAssert.notNull(intention, ApiCode.NOT_FOUND, "意向不存在");
    return ApiResponse.ok(intention);
  }

  /** 分页查询融资意向列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("enterpriseId") Long enterpriseId,
                          @QueryParam("userId") Long userId,
                          @QueryParam("status") String status,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<FinancingIntention> intentions = intentionService.list(enterpriseId, userId, status,
        bounds.offset(), bounds.size());
    long total = intentionService.count(enterpriseId, userId, status);
    return ApiResponse.ok(PageResult.of(intentions, total, bounds.page(), bounds.size()));
  }

  /** 创建融资意向 */
  @POST
  public ApiResponse create(FinancingIntention intention) {
    Long id = intentionService.create(intention);
    return ApiResponse.ok(id);
  }

  /** 更新融资意向 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, FinancingIntention intention) {
    ApiAssert.isTrue(intentionService.update(intention.withId(id)), ApiCode.NOT_FOUND, "意向不存在");
    return ApiResponse.ok(true);
  }

  /** 删除融资意向 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(intentionService.delete(id), ApiCode.NOT_FOUND, "意向不存在");
    return ApiResponse.ok(true);
  }

  /** 更新融资意向状态 */
  @POST
  @Path("/{id}/status")
  public ApiResponse updateStatus(@PathParam("id") Long id,
                                  UpdateIntentionStatusRequest request) {
    ApiAssert.isTrue(intentionService.updateStatus(id, request.status(), request.refusalReason()),
        ApiCode.NOT_FOUND, "意向不存在");
    return ApiResponse.ok(true);
  }

  /** 绑定融资意向目标产品 */
  @POST
  @Path("/{id}/target-product/{productId}")
  public ApiResponse updateTargetProduct(@PathParam("id") Long id, @PathParam("productId") Long productId) {
    ApiAssert.isTrue(intentionService.updateTargetProduct(id, productId), ApiCode.NOT_FOUND, "意向不存在");
    return ApiResponse.ok(true);
  }
}
