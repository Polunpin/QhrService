package com.qhr.dto;

import com.qhr.model.WithId;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 修改信贷产品请求。
 */
@Deprecated
public record CreditProductRequest(Long id,
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
                                   Integer status) implements Serializable, WithId<CreditProductRequest> {

  /** 复制并替换id。 */
  public CreditProductRequest withId(Long id) {
    return new CreditProductRequest(id, bankName, productName, productType, minAmount, maxAmount,
        interestRateRange, loanTerm, creditValidity, disbursementAccount, scene, online,
        repaymentMethod, status);
  }
}
