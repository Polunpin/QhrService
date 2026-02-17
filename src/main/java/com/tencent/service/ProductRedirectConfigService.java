package com.tencent.service;

import com.tencent.model.ProductRedirectConfig;

import java.util.List;

public interface ProductRedirectConfigService {

  /** 根据ID查询配置 */
  ProductRedirectConfig getById(Long id);

  /** 创建配置并返回主键 */
  Long create(ProductRedirectConfig config);

  /** 更新配置 */
  boolean update(ProductRedirectConfig config);

  /** 删除配置 */
  boolean delete(Long id);

  /** 更新启用状态 */
  boolean updateActive(Long id, Integer isActive);

  /** 累计点击次数+1 */
  boolean increaseClickCount(Long id);

  /** 分页查询配置列表 */
  List<ProductRedirectConfig> list(Long productId,
                                   String configType,
                                   String externalUserId,
                                   Integer isActive,
                                   String targetName,
                                   Integer offset,
                                   Integer size);

  /** 统计配置数量 */
  long count(Long productId,
             String configType,
             String externalUserId,
             Integer isActive,
             String targetName);

  /** 查询产品下启用配置 */
  List<ProductRedirectConfig> listActiveByProductId(Long productId);
}
