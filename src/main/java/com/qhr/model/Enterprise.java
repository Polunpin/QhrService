package com.qhr.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业实体。
 */
@Data
@TableName("jc_enterprise")
public class Enterprise implements Serializable {

  @TableId(type = IdType.AUTO)
  private Long id;
  /*企业名称*/
  private String name;
  /*统一社会信用代码*/
  @TableField("credit_code")
  private String creditCode;
  /*成立日期*/
  @TableField("start_date")
  private String startDate;
  /*法定代表人姓名*/
  @TableField("oper_name")
  private String operName;
  /*状态*/
  private String status;
  /*注册地址*/
  private String address;
  @TableField("created_at")
  private LocalDateTime createdAt;
  @TableField("updated_at")
  private LocalDateTime updatedAt;

}
