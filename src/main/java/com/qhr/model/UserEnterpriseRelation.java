package com.qhr.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-企业关系实体。
 */
public record UserEnterpriseRelation(Long id,
                                     Long enterpriseId,
                                     String userOpenId,
                                     String role,
                                     LocalDateTime createdAt) implements Serializable {

}
