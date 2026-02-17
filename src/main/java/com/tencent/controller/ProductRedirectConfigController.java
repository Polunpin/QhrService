package com.tencent.controller;

import com.tencent.config.ApiAssert;
import com.tencent.config.ApiCode;
import com.tencent.config.ApiResponse;
import com.tencent.config.PageBounds;
import com.tencent.config.PageResult;
import com.tencent.dto.UpdateStatusRequest;
import com.tencent.model.ProductRedirectConfig;
import com.tencent.service.ProductRedirectConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-redirect-configs")
public class ProductRedirectConfigController {

  private final ProductRedirectConfigService productRedirectConfigService;

  public ProductRedirectConfigController(@Autowired ProductRedirectConfigService productRedirectConfigService) {
    this.productRedirectConfigService = productRedirectConfigService;
  }

  /** 查询配置详情 */
  @GetMapping("/{id}")
  public ApiResponse getById(@PathVariable Long id) {
    ProductRedirectConfig config = productRedirectConfigService.getById(id);
    ApiAssert.notNull(config, ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(config);
  }

  /** 分页查询配置 */
  @GetMapping("/list")
  public ApiResponse list(@RequestParam(required = false) Long productId,
                          @RequestParam(required = false) String configType,
                          @RequestParam(required = false) String externalUserId,
                          @RequestParam(required = false) Integer isActive,
                          @RequestParam(required = false) String targetName,
                          @RequestParam(required = false) Integer page,
                          @RequestParam(required = false) Integer size) {
    PageBounds bounds = PageBounds.of(page, size);
    List<ProductRedirectConfig> items = productRedirectConfigService.list(productId, configType, externalUserId,
        isActive, targetName, bounds.offset(), bounds.size());
    long total = productRedirectConfigService.count(productId, configType, externalUserId, isActive, targetName);
    return ApiResponse.ok(PageResult.of(items, total, bounds.page(), bounds.size()));
  }

  /** 查询产品下启用配置 */
  @GetMapping("/product/{productId}/active")
  public ApiResponse listActiveByProductId(@PathVariable Long productId) {
    return ApiResponse.ok(productRedirectConfigService.listActiveByProductId(productId));
  }

  /** 创建配置 */
  @PostMapping
  public ApiResponse create(@RequestBody ProductRedirectConfig config) {
    Long id = productRedirectConfigService.create(config);
    return ApiResponse.ok(id);
  }

  /** 更新配置 */
  @PutMapping("/{id}")
  public ApiResponse update(@PathVariable Long id, @RequestBody ProductRedirectConfig config) {
    ApiAssert.isTrue(productRedirectConfigService.update(config.withId(id)), ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }

  /** 删除配置 */
  @DeleteMapping("/{id}")
  public ApiResponse delete(@PathVariable Long id) {
    ApiAssert.isTrue(productRedirectConfigService.delete(id), ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }

  /** 更新启用状态 */
  @PostMapping("/{id}/active")
  public ApiResponse updateActive(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
    ApiAssert.isTrue(productRedirectConfigService.updateActive(id, request.status()),
        ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }

  /** 点击计数+1 */
  @PostMapping("/{id}/click")
  public ApiResponse increaseClickCount(@PathVariable Long id) {
    ApiAssert.isTrue(productRedirectConfigService.increaseClickCount(id),
        ApiCode.NOT_FOUND, "产品跳转配置不存在");
    return ApiResponse.ok(true);
  }
}
