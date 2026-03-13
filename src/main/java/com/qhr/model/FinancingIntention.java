package com.qhr.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 融资意向实体。
 */
public record FinancingIntention(Long id,
                                 String applicationNo,
                                 Long enterpriseId,
                                 Long userId,
                                 BigDecimal expectedAmount,
                                 Integer expectedTerm,
                                 String purpose,
                                 String repaymentSource,
                                 String guaranteeType,
                                 Long targetProductId,
                                 String contactMobile,
                                 String status,
                                 String refusalReason,
                                 Integer urgencyLevel,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) implements Serializable, WithId<FinancingIntention> {

  /** 复制并替换id。 */
  public FinancingIntention withId(Long id) {
    return new FinancingIntention(id, applicationNo, enterpriseId, userId, expectedAmount, expectedTerm,
        purpose, repaymentSource, guaranteeType, targetProductId, contactMobile, status, refusalReason,
        urgencyLevel, createdAt, updatedAt);
  }
}
