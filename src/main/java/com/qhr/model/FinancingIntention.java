package com.qhr.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 融资需求实体。
 */
@Data
@TableName("yw_financing_intentions")
public class FinancingIntention implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;
  @TableField("user_open_id")
  private String userOpenId;
  @TableField("enterprise_id")
  private Long enterpriseId;
  @TableField("amount_range")
  private String amountRange;
  @TableField("personal_credit_name")
  private String personalCreditName;
  @TableField("personal_credit_cloud_id")
  private String personalCreditCloudId;
  @TableField("enterprise_credit_name")
  private String enterpriseCreditName;
  @TableField("enterprise_credit_cloud_id")
  private String enterpriseCreditCloudId;
  @TableField("tax_account")
  private String taxAccount;
  @TableField("tax_password")
  private String taxPassword;
  @TableField("created_at")
  private LocalDateTime createdAt;
  @TableField("updated_at")
  private LocalDateTime updatedAt;
}
