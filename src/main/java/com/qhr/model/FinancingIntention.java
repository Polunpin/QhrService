package com.qhr.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 融资需求实体。
 */
@TableName("yw_financing_intentions")
public class FinancingIntention implements Serializable, WithId<FinancingIntention> {

  @TableId(type = IdType.AUTO)
  private Long id;
  @TableField("enterprise_id")
  private Long enterpriseId;
  @TableField("amount_range")
  private String amountRange;
  private Boolean property;
  @TableField("property_mortgage")
  private Boolean propertyMortgage;
  @TableField("spouse_support")
  private Boolean spouseSupport;
  @TableField("tax_account")
  private String taxAccount;
  @TableField("tax_password")
  private String taxPassword;
  @TableField("created_at")
  private LocalDateTime createdAt;
  @TableField("updated_at")
  private LocalDateTime updatedAt;

  public FinancingIntention() {
  }

  public FinancingIntention(Long id, Long enterpriseId, String amountRange, Boolean property, Boolean propertyMortgage,
                            Boolean spouseSupport, String taxAccount, String taxPassword,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.enterpriseId = enterpriseId;
    this.amountRange = amountRange;
    this.property = property;
    this.propertyMortgage = propertyMortgage;
    this.spouseSupport = spouseSupport;
    this.taxAccount = taxAccount;
    this.taxPassword = taxPassword;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getEnterpriseId() {
    return enterpriseId;
  }

  public void setEnterpriseId(Long enterpriseId) {
    this.enterpriseId = enterpriseId;
  }

  public String getAmountRange() {
    return amountRange;
  }

  public void setAmountRange(String amountRange) {
    this.amountRange = amountRange;
  }

  public Boolean getProperty() {
    return property;
  }

  public void setProperty(Boolean property) {
    this.property = property;
  }

  public Boolean getPropertyMortgage() {
    return propertyMortgage;
  }

  public void setPropertyMortgage(Boolean propertyMortgage) {
    this.propertyMortgage = propertyMortgage;
  }

  public Boolean getSpouseSupport() {
    return spouseSupport;
  }

  public void setSpouseSupport(Boolean spouseSupport) {
    this.spouseSupport = spouseSupport;
  }

  public String getTaxAccount() {
    return taxAccount;
  }

  public void setTaxAccount(String taxAccount) {
    this.taxAccount = taxAccount;
  }

  public String getTaxPassword() {
    return taxPassword;
  }

  public void setTaxPassword(String taxPassword) {
    this.taxPassword = taxPassword;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Long id() {
    return id;
  }

  public Long enterpriseId() {
    return enterpriseId;
  }

  public String amountRange() {
    return amountRange;
  }

  public Boolean property() {
    return property;
  }

  public Boolean propertyMortgage() {
    return propertyMortgage;
  }

  public Boolean spouseSupport() {
    return spouseSupport;
  }

  public String taxAccount() {
    return taxAccount;
  }

  public String taxPassword() {
    return taxPassword;
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  public LocalDateTime updatedAt() {
    return updatedAt;
  }

  @Override
  public FinancingIntention withId(Long id) {
    return new FinancingIntention(id, enterpriseId, amountRange, property, propertyMortgage,
        spouseSupport, taxAccount, taxPassword, createdAt, updatedAt);
  }
}
