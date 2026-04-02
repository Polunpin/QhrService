package com.qhr.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-企业关系实体。
 */
@Data
@TableName("zj_user_enterprise_relation")
public class UserEnterpriseRelation implements Serializable {

  /**
   * 主键ID。
   */
  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 关联企业ID。
   */
  @TableField("enterprise_id")
  private Long enterpriseId;

  /**
   * 关联用户OpenID。
   */
  @TableField("user_open_id")
  private String userOpenId;

  /**
   * 用户在企业下的角色，当前业务暂未使用。
   */
  @Deprecated
  private String role;

  /**
   * 关系创建时间。
   */
  @TableField("created_at")
  private LocalDateTime createdAt;
  @TableLogic(value = "0", delval = "1")
  private Integer deleted;

  public UserEnterpriseRelation() {
  }

  public UserEnterpriseRelation(Long enterpriseId, String userOpenId) {
    this.enterpriseId = enterpriseId;
    this.userOpenId = userOpenId;
  }

}
