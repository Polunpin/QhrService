package com.tencent.controller;

import com.tencent.config.ApiResponse;
import com.tencent.vo.CreditProductStats;
import com.tencent.dto.MatchProductsRequest;
import com.tencent.dto.UpdateProductStatusRequest;
import com.tencent.model.CreditProduct;
import com.tencent.service.CreditProductService;
import com.tencent.vo.CreditProducts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.tencent.config.ApiAssert;
import com.tencent.config.ApiCode;
import com.tencent.config.PageBounds;
import com.tencent.config.PageResult;

@RestController
@RequestMapping("/api/products")
public class CreditProductController {

  private final CreditProductService creditProductService;

  public CreditProductController(@Autowired CreditProductService creditProductService) {
    this.creditProductService = creditProductService;
  }

  /** 查询产品详情 */
  @GetMapping("/{id}")
  public ApiResponse getById(@PathVariable Long id) {
    CreditProduct product = creditProductService.getById(id);
    ApiAssert.notNull(product, ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(product);
  }

  /** 产品统计查询 */
  @GetMapping("/stats")
  public ApiResponse getStats() {
    CreditProductStats productStats = creditProductService.getStats();
    ApiAssert.notNull(productStats, ApiCode.NOT_FOUND, "产品统计失败");
    return ApiResponse.ok(productStats);
  }

  /** 分页查询产品列表 */
  @GetMapping("/list")
  public ApiResponse list(@RequestParam(required = false) Integer status,
                          @RequestParam(required = false) String productType,
                          @RequestParam(required = false) String bankName,
                          @RequestParam(required = false) Integer page,
                          @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<CreditProducts> products = creditProductService.list(status, productType, bankName,
        bounds.offset(), bounds.size());
    long total = creditProductService.count(status, productType, bankName);
    return ApiResponse.ok(PageResult.of(products, total, bounds.page(), bounds.size()));
  }

  /** 创建产品 */
  @PostMapping
  public ApiResponse create(@RequestBody CreditProduct product) {
    Long id = creditProductService.create(product);
    return ApiResponse.ok(id);
  }

  /** 更新产品 */
  @PutMapping("/{id}")
  public ApiResponse update(@PathVariable Long id, @RequestBody CreditProduct product) {
    ApiAssert.isTrue(creditProductService.update(product.withId(id)), ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(true);
  }

  /** 删除产品 */
  @DeleteMapping("/{id}")
  public ApiResponse delete(@PathVariable Long id) {
    ApiAssert.isTrue(creditProductService.delete(id), ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(true);
  }

  /** 更新产品状态 */
  @PostMapping("/{id}/status")
  public ApiResponse updateStatus(@PathVariable Long id,
                                  @RequestBody UpdateProductStatusRequest request) {
    ApiAssert.isTrue(creditProductService.updateStatus(id, request.status()),
        ApiCode.NOT_FOUND, "产品不存在");
    return ApiResponse.ok(true);
  }

  /** 按融资条件匹配产品 */
  @PostMapping("/match")
  public ApiResponse matchProducts(@RequestBody MatchProductsRequest request,
                                   @RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<CreditProduct> products = creditProductService.findEligibleProducts(
        request.expectedAmount(), request.expectedTerm(), request.regionCode(), request.productType(),
        bounds.offset(), bounds.size());
    long total = creditProductService.countEligibleProducts(
        request.expectedAmount(), request.expectedTerm(), request.regionCode(), request.productType());
    return ApiResponse.ok(PageResult.of(products, total, bounds.page(), bounds.size()));
  }
}
