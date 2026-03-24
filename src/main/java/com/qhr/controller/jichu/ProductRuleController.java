package com.qhr.controller.jichu;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiResponse;
import com.qhr.config.PageBounds;
import com.qhr.config.PageResult;
import com.qhr.model.ProductRule;
import com.qhr.service.ProductRuleService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/*产品规则*/
@ApplicationScoped
@Path("/api/product-rules")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductRuleController {

  private final ProductRuleService productRuleService;

  public ProductRuleController(ProductRuleService productRuleService) {
    this.productRuleService = productRuleService;
  }

  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ProductRule productRule = productRuleService.getById(id);
    ApiAssert.notNull(productRule, ApiCode.NOT_FOUND, "产品规则不存在");
    return ApiResponse.ok(productRule);
  }

  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("productId") Long productId,
                          @QueryParam("ruleVersion") Integer ruleVersion,
                          @QueryParam("ruleName") String ruleName,
                          @QueryParam("isActive") Integer isActive,
                          @QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<ProductRule> items = productRuleService.list(productId, ruleVersion, ruleName, isActive,
        bounds.offset(), bounds.size());
    long total = productRuleService.count(productId, ruleVersion, ruleName, isActive);
    return ApiResponse.ok(PageResult.of(items, total, bounds.page(), bounds.size()));
  }

  @POST
  public ApiResponse create(ProductRule productRule) {
    ApiAssert.notNull(productRule, ApiCode.BAD_REQUEST, "请求体不能为空");
    return ApiResponse.ok(productRuleService.create(productRule));
  }

  @PUT
  @Path("/{id}")
  public ApiResponse update(@PathParam("id") Long id, ProductRule productRule) {
    ApiAssert.notNull(productRule, ApiCode.BAD_REQUEST, "请求体不能为空");
    ApiAssert.isTrue(productRuleService.update(productRule.withId(id)),
        ApiCode.NOT_FOUND, "产品规则不存在");
    return ApiResponse.ok(true);
  }

  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.isTrue(productRuleService.delete(id), ApiCode.NOT_FOUND, "产品规则不存在");
    return ApiResponse.ok(true);
  }
}
