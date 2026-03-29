package com.qhr.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 信贷产品实体。
 * TODO 后期追加两个：贷款速度、通过率
 */
@Data
public class Product {
    Long id;
    String bankName;
    String productName;
    String productType;
    @Deprecated
    BigDecimal minAmount;
    BigDecimal maxAmount;
    //TODO 确认业务是否需要rate_min和rate_max
    String interestRateRange;
    Integer loanTerm;
    Integer creditValidity;
    String disbursementAccount;
    Integer scene;
    Integer online;
    String repaymentMethod;
    Integer status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
