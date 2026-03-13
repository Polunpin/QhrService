package com.qhr.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-企业关系实体。
 */
public record UserEnterpriseRelation(Long id,
                                     Long enterpriseId,
                                     Long userId,
                                     String role,
                                     LocalDateTime createdAt) implements Serializable, WithId<UserEnterpriseRelation> {

  /** 复制并替换id。 */
  public UserEnterpriseRelation withId(Long id) {
    return new UserEnterpriseRelation(id, enterpriseId, userId, role, createdAt);
  }
}
