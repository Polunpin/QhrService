package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.ProductRule;
import com.qhr.service.ProductRuleService;
import com.qhr.service.ProductRuleTemplateService;
import jakarta.ws.rs.*;

/*产品规则*/
@Path("/api/product-rules")
public class ProductRuleController {

  private final ProductRuleService productRuleService;
  private final ProductRuleTemplateService productRuleTemplateService;

  public ProductRuleController(ProductRuleService productRuleService,
                               ProductRuleTemplateService productRuleTemplateService) {
    this.productRuleService = productRuleService;
    this.productRuleTemplateService = productRuleTemplateService;
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

  /**
   * 查询内置规则模板草稿。
   * 主要用于把 Excel 规则先结构化成 ProductRule + ext_json，再决定是否入库。
   */
  @GET
  @Path("/builtin-drafts")
  public ApiResponse builtInDrafts(@QueryParam("bankName") String bankName,
                                   @QueryParam("productName") String productName) {
    if ((bankName == null || bankName.isBlank()) && (productName == null || productName.isBlank())) {
      return ApiResponse.ok(productRuleTemplateService.listBuiltInTemplates());
    }
    ApiAssert.isTrue(bankName != null && !bankName.isBlank(), ApiCode.BAD_REQUEST, "bankName不能为空");
    ApiAssert.isTrue(productName != null && !productName.isBlank(), ApiCode.BAD_REQUEST, "productName不能为空");
    Object draft = productRuleTemplateService.findBuiltInTemplate(bankName, productName);
    ApiAssert.notNull(draft, ApiCode.NOT_FOUND, "内置规则模板不存在");
    return ApiResponse.ok(draft);
  }
}
