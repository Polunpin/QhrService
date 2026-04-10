package com.qhr.model;

import com.baomidou.mybatisplus.annotation.*;
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
  @TableField("created_at")
  private LocalDateTime createdAt;
  @TableField("updated_at")
  private LocalDateTime updatedAt;
  @TableLogic(value = "0", delval = "1")
  private Integer deleted;
}
