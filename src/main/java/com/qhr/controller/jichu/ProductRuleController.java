package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.ProductRule;
import com.qhr.service.ProductRuleService;
import jakarta.ws.rs.*;

/*产品规则*/
@Path("/api/product-rules")
public class ProductRuleController {

  private final ProductRuleService productRuleService;

  public ProductRuleController(ProductRuleService productRuleService) {
    this.productRuleService = productRuleService;
  }

  /**
   * 分页查询产品规则列表
   */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("page") Integer page, @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    PageResult<ProductRule> items = productRuleService.list(bounds.offset(), bounds.size());
    return ApiResponse.ok(items);
  }

  /** 创建产品规则 */
  @POST
  public ApiResponse create(ProductRule productRule) {
    ApiAssert.notNull(productRule, ApiCode.BAD_REQUEST, "请求体productRule不能为空");
    return ApiResponse.ok(productRuleService.create(productRule));
  }

  /** 更新产品规则 */
  @PUT
  public ApiResponse update(ProductRule productRule) {
    ApiAssert.notNull(productRule, ApiCode.BAD_REQUEST, "请求体productRule不能为空");
    return ApiResponse.ok(productRuleService.update(productRule));
  }

  /** 删除产品规则 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(productRuleService.delete(id));
  }

  /**
   * 查询产品规则详情
   */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(productRuleService.getById(id));
  }
}
