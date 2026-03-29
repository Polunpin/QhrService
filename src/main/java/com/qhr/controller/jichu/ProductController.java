package com.qhr.controller.jichu;

import com.qhr.config.*;
import com.qhr.model.Product;
import com.qhr.service.CreditProductService;
import jakarta.ws.rs.*;

/*产品管理*/
@Path("/api/products")
public class ProductController {

  private final CreditProductService creditProductService;
  public ProductController(CreditProductService creditProductService) {
    this.creditProductService = creditProductService;
  }

  /** 分页查询产品列表 */
  @GET
  @Path("/list")
  public ApiResponse list(@QueryParam("page") Integer page,
                          @QueryParam("size") Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    PageResult<Product> products = creditProductService.list(bounds.offset(), bounds.size());
    return ApiResponse.ok(products);
  }

  /** 创建产品 */
  @POST
  public ApiResponse create(Product product) {
    ApiAssert.notNull(product, ApiCode.BAD_REQUEST, "请求体product不能为空");
    return ApiResponse.ok(creditProductService.create(product));
  }

  /** 更新产品 */
  @PUT
  @Path("/{id}")
  public ApiResponse update(Product product) {
    ApiAssert.notNull(product, ApiCode.BAD_REQUEST, "请求体product不能为空");
    return ApiResponse.ok(creditProductService.update(product));
  }

  /** 删除产品 */
  @DELETE
  @Path("/{id}")
  public ApiResponse delete(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(creditProductService.delete(id));
  }

  /**
   * 查询产品详情
   */
  @GET
  @Path("/{id}")
  public ApiResponse getById(@PathParam("id") Long id) {
    ApiAssert.notNull(id, ApiCode.BAD_REQUEST, "id不能为空");
    return ApiResponse.ok(creditProductService.getById(id));
  }

}
