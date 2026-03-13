package com.qhr.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
                            Integer creditValidity,
                            String disbursementAccount,
                            Integer scene,
                            Integer online,
                            String repaymentMethod,
                            Integer status,
                            LocalDateTime createdAt,
                            LocalDateTime updatedAt) implements Serializable, WithId<CreditProduct> {

  /** 复制并替换id。 */
  public CreditProduct withId(Long id) {
    return new CreditProduct(id, bankName, productName, productType, minAmount, maxAmount,
        interestRateRange, loanTerm, creditValidity, disbursementAccount, scene, online,
        repaymentMethod, status, createdAt, updatedAt);
  }
}
