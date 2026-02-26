package com.tencent.dto;

import com.tencent.model.WithId;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 修改信贷产品请求。
 */
public record CreditProductRequest(Long id,
                                   String bankName,
                                   String productName,
                                   String productType,
                                   BigDecimal minAmount,
                                   BigDecimal maxAmount,
                                   String interestRateRange,
                                   Integer loanTerm,
                                   String repaymentMethod,
                                   String region,
//                                   String criteriaJson,
                                   Integer status,
                                   BigDecimal successRate) implements Serializable, WithId<CreditProductRequest> {

  /** 复制并替换id。 */
  public CreditProductRequest withId(Long id) {
    return new CreditProductRequest(id, bankName, productName, productType, minAmount, maxAmount,
        interestRateRange, loanTerm, repaymentMethod, region, status, successRate);
  }
}
