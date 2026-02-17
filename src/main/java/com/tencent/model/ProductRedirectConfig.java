package com.tencent.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 信贷产品申请路径配置实体。
 */
public record ProductRedirectConfig(Long id,
                                    Long productId,
                                    String configType,
                                    String externalUserId,
                                    String targetName,
                                    String redirectUrl,
                                    Integer isActive,
                                    Integer clickCount,
                                    Integer priority,
                                    LocalDateTime lastUpdateAt,
                                    LocalDateTime createdAt) implements Serializable, WithId<ProductRedirectConfig> {

  /** 复制并替换id。 */
  public ProductRedirectConfig withId(Long id) {
    return new ProductRedirectConfig(id, productId, configType, externalUserId, targetName,
        redirectUrl, isActive, clickCount, priority, lastUpdateAt, createdAt);
  }
}
