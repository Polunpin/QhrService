package com.qhr.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 融资需求实体。
 */
public record FinancingIntention(Long id,
                                 Long enterpriseId,
                                 String amountRange,
                                 Boolean property,
                                 Boolean propertyMortgage,
                                 Boolean spouseSupport,
                                 String taxAccount,
                                 String taxPassword,
                                 LocalDateTime createdAt,
                                 LocalDateTime updatedAt) implements Serializable, WithId<FinancingIntention> {

  /** 复制并替换id。 */
  public FinancingIntention withId(Long id) {
    return new FinancingIntention(id, enterpriseId, amountRange, property, propertyMortgage,
        spouseSupport, taxAccount, taxPassword, createdAt, updatedAt);
  }
}
