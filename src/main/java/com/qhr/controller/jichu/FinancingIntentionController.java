package com.qhr.controller.jichu;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.config.PageBounds;
import com.qhr.model.FinancingIntention;
import com.qhr.service.FinancingIntentionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/*融资需求管理*/
@ApplicationScoped
@Path("/api/financing-intentions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FinancingIntentionController {

  private final FinancingIntentionService financingIntentionService;
  public FinancingIntentionController(FinancingIntentionService financingIntentionService) {
    this.financingIntentionService = financingIntentionService;
  }

  /** 分页查询融资需求列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("enterpriseId") Long enterpriseId,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<FinancingIntention> intentions = financingIntentionService.list(enterpriseId, bounds.offset(), bounds.size());
    return ApiResponse.ok(intentions);
  }

  /** 创建融资需求 */
  @POST
  public ApiResponse create(FinancingIntention intention) {
    ApiAssert.notNull(intention, ApiCode.BAD_REQUEST, "请求体intention不能为空");
    return ApiResponse.ok(financingIntentionService.create(intention));
  }

  /** 更新融资需求 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, FinancingIntention intention) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    ApiAssert.notNull(intention, ApiCode.BAD_REQUEST, "请求体intention不能为空");
    return ApiResponse.ok(financingIntentionService.update(intention));
  }

  /** 删除融资需求 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(financingIntentionService.delete(id));
  }

  /**
   * 查询融资需求详情
   */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(financingIntentionService.getById(id));
  }
}
