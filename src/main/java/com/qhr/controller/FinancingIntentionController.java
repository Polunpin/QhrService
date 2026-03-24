package com.qhr.controller;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.config.PageBounds;
import com.qhr.config.PageResult;
import com.qhr.model.FinancingIntention;
import com.qhr.service.FinancingIntentionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

  /** 查询融资需求详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    FinancingIntention intention = financingIntentionService.getById(id);
    ApiAssert.notNull(intention, ApiCode.NOT_FOUND, "融资需求不存在");
    return ApiResponse.ok(intention);
  }

  /** 分页查询融资需求列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("enterpriseId") Long enterpriseId,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<FinancingIntention> intentions = financingIntentionService.list(enterpriseId, bounds.offset(), bounds.size());
    long total = financingIntentionService.count(enterpriseId);
    return ApiResponse.ok(PageResult.of(intentions, total, bounds.page(), bounds.size()));
  }

  /** 创建融资需求 */
  @POST
  public ApiResponse create(FinancingIntention intention) {
    Long id = financingIntentionService.create(intention);
    return ApiResponse.ok(id);
  }

  /** 更新融资需求 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, FinancingIntention intention) {
    ApiAssert.isTrue(financingIntentionService.update(intention.withId(id)), ApiCode.NOT_FOUND, "融资需求不存在");
    return ApiResponse.ok(true);
  }

  /** 删除融资需求 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(financingIntentionService.delete(id), ApiCode.NOT_FOUND, "融资需求不存在");
    return ApiResponse.ok(true);
  }
}
