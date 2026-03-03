package com.tencent.controller;

import com.tencent.config.*;
import com.tencent.dto.UpdateStatusRequest;
import com.tencent.model.ProductRedirectConfig;
import com.tencent.service.ProductRedirectConfigService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path("/api/product-redirect-configs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductRedirectConfigController {

  private final ProductRedirectConfigService productRedirectConfigService;
  public ProductRedirectConfigController(ProductRedirectConfigService productRedirectConfigService) {
    this.productRedirectConfigService = productRedirectConfigService;
  }

  /** 查询配置详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ProductRedirectConfig config = productRedirectConfigService.getById(id);
    ApiAssert.notNull(config, ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(config);
  }

  /** 分页查询配置 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("productId") Long productId,
                          @QueryParam("configType") String configType,
                          @QueryParam("externalUserId") String externalUserId,
                          @QueryParam("isActive") Integer isActive,
                          @QueryParam("targetName") String targetName,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<ProductRedirectConfig> items = productRedirectConfigService.list(productId, configType, externalUserId,
        isActive, targetName, bounds.offset(), bounds.size());
    long total = productRedirectConfigService.count(productId, configType, externalUserId, isActive, targetName);
    return ApiResponse.ok(PageResult.of(items, total, bounds.page(), bounds.size()));
  }

  /** 查询产品下启用配置 */
  @GET
  @Path("/product/{productId}/active")
  public ApiResponse listActiveByProductId(@PathParam("productId") Long productId) {
    return ApiResponse.ok(productRedirectConfigService.listActiveByProductId(productId));
  }

  /** 创建配置 */
  @POST
  public ApiResponse create(ProductRedirectConfig config) {
    Long id = productRedirectConfigService.create(config);
    return ApiResponse.ok(id);
  }

  /** 更新配置 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, ProductRedirectConfig config) {
    ApiAssert.isTrue(productRedirectConfigService.update(config.withId(id)), ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }

  /** 删除配置 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(productRedirectConfigService.delete(id), ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }

  /** 更新启用状态 */
  @POST
  @Path("/{id}/active")
  public ApiResponse updateActive(@PathParam("id") Long id, UpdateStatusRequest request) {
    ApiAssert.isTrue(productRedirectConfigService.updateActive(id, request.status()),
        ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }

  /** 点击计数+1 */
  @POST
  @Path("/{id}/click")
  public ApiResponse increaseClickCount(@PathParam("id") Long id) {
    ApiAssert.isTrue(productRedirectConfigService.increaseClickCount(id),
        ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }
}
