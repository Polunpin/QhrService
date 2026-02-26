package com.tencent.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业实体。
 */
public record Enterprise(Long id,
                         /*企业全称*/
                         String fullName,
                         /*统一社会信用代码*/
                         String creditCode,
                         /*所属行业*/
                         String industry,
                         /*纳税评级(A/B/M/C/D)*/
                         String taxRating,
                         /*地区编码*/
                         String regionCode,
                         /*年营业额(万元)*/
                         BigDecimal annualTurnover,
                         /*年纳税额(万元)*/
                         BigDecimal annualTaxAmount,
                         /*现有贷款余额(万元)*/
                         BigDecimal existingLoanBalance,
                         /*匹配状态*/
                         String matchStatus,
                         /*画像详情(纳税、社保等)*/
                         String profileData,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) implements Serializable, WithId<Enterprise> {

  /** 复制并替换id。 */
  public Enterprise withId(Long id) {
    return new Enterprise(id, fullName, creditCode, industry, taxRating, regionCode,
        annualTurnover, annualTaxAmount, existingLoanBalance, matchStatus, profileData,
        createdAt, updatedAt);
  }
}
