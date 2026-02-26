package com.tencent.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 信贷产品实体。
 */
public record CreditProduct(Long id,
                            String bankName,
                            String productName,
                            String productType,
                            BigDecimal minAmount,
                            BigDecimal maxAmount,
                            String interestRateRange,
                            Integer loanTerm,
                            String repaymentMethod,
                            String region,
                            String criteriaJson,
                            Integer status,
                            BigDecimal successRate,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) implements Serializable, WithId<CreditProduct> {

  /** 复制并替换id。 */
  public CreditProduct withId(Long id) {
    return new CreditProduct(id, bankName, productName, productType, minAmount, maxAmount,
        interestRateRange, loanTerm, repaymentMethod, region, criteriaJson, status,
        successRate, createdAt, updatedAt);
  }
}
