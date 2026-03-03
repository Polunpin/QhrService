package com.tencent.controller;

import com.tencent.config.*;
import com.tencent.dto.MatchProductsRequest;
import com.tencent.dto.UpdateProductStatusRequest;
import com.tencent.model.CreditProduct;
import com.tencent.service.CreditProductService;
import com.tencent.vo.CreditProductStats;
import com.tencent.vo.CreditProducts;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@ApplicationScoped
@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CreditProductController {

  private final CreditProductService creditProductService;
  public CreditProductController(CreditProductService creditProductService) {
    this.creditProductService = creditProductService;
  }

  /** 查询产品详情 */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    CreditProduct product = creditProductService.getById(id);
    ApiAssert.notNull(product, ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(product);
  }

  /** 产品统计查询 */
  @GET
  @Path("/stats")
  public ApiResponse getStats() {
    CreditProductStats productStats = creditProductService.getStats();
    ApiAssert.notNull(productStats, ApiCode.NOT_FOUND, "产品统计失败");
    return ApiResponse.ok(productStats);
  }

  /** 分页查询产品列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("status") Integer status,
                          @QueryParam("productType") String productType,
                          @QueryParam("bankName") String bankName,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<CreditProducts> products = creditProductService.list(status, productType, bankName,
        bounds.offset(), bounds.size());
    long total = creditProductService.count(status, productType, bankName);
    return ApiResponse.ok(PageResult.of(products, total, bounds.page(), bounds.size()));
  }

  /** 创建产品 */
  @POST
  public ApiResponse create(CreditProduct product) {
    Long id = creditProductService.create(product);
    return ApiResponse.ok(id);
  }

  /** 更新产品 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, CreditProduct product) {
    ApiAssert.isTrue(creditProductService.update(product.withId(id)), ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(true);
  }

  /** 删除产品 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(creditProductService.delete(id), ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(true);
  }

  /** 更新产品状态 */
  @POST
  @Path("/{id}/status")
  public ApiResponse updateStatus(@PathParam("id") Long id,
                                  UpdateProductStatusRequest request) {
    ApiAssert.isTrue(creditProductService.updateStatus(id, request.status()),
        ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(true);
  }

  /** 按融资条件匹配产品 */
  @POST
  @Path("/match")
  public ApiResponse matchProducts(MatchProductsRequest request,
                                   @QueryParam("page") Integer page,
                                   @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<CreditProduct> products = creditProductService.findEligibleProducts(
        request.expectedAmount(), request.expectedTerm(), request.regionCode(), request.productType(),
        bounds.offset(), bounds.size());
    long total = creditProductService.countEligibleProducts(
        request.expectedAmount(), request.expectedTerm(), request.regionCode(), request.productType());
    return ApiResponse.ok(PageResult.of(products, total, bounds.page(), bounds.size()));
  }
}
