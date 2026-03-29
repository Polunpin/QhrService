package com.qhr.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内部员工实体。
 */
@Data
@TableName("jc_staffs")
public class Staff implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;
  private String openid;
  @JsonProperty("name")
  @TableField("real_name")
  private String realName;
  @JsonProperty("phone")
  private String mobile;
  private String role;
  private String department;
  private Integer status;
  @TableField("last_update_at")
  private LocalDateTime lastUpdateAt;
  @TableField("created_at")
  private LocalDateTime createdAt;

}
