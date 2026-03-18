package com.qhr.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信贷产品实体。
 * TODO 后期追加两个：贷款速度、通过率
 */
public record Product(Long id,
                      String bankName,
                      String productName,
                      String productType,
                      @Deprecated
                            BigDecimal minAmount,
                      BigDecimal maxAmount,
                      //TODO 确认业务是否需要rate_min和rate_max
                      String interestRateRange,
                      Integer loanTerm,
                      Integer creditValidity,
                      String disbursementAccount,
                      Integer scene,
                      Integer online,
                      String repaymentMethod,
                      Integer status,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt) implements Serializable, WithId<Product> {

  /** 复制并替换id。 */
  public Product withId(Long id) {
    return new Product(id, bankName, productName, productType, minAmount, maxAmount,
        interestRateRange, loanTerm, creditValidity, disbursementAccount, scene, online,
        repaymentMethod, status, createdAt, updatedAt);
  }
}
