package com.qhr.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务流转日志实体。
 */
@TableName("yw_status_logs")
public class StatusLog implements Serializable, WithId<StatusLog> {

  @TableId(type = IdType.AUTO)
  private Long id;
  @TableField("order_id")
  private Long orderId;
  @TableField("operator_type")
  private String operatorType;
  @TableField("operator_id")
  private Long operatorId;
  @TableField("pre_stage")
  private String preStage;
  @TableField("post_stage")
  private String postStage;
  private String remark;
  @TableField("created_at")
  private LocalDateTime createdAt;

  public StatusLog() {
  }

  public StatusLog(Long id, Long orderId, String operatorType, Long operatorId, String preStage, String postStage,
                   String remark, LocalDateTime createdAt) {
    this.id = id;
    this.orderId = orderId;
    this.operatorType = operatorType;
    this.operatorId = operatorId;
    this.preStage = preStage;
    this.postStage = postStage;
    this.remark = remark;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public String getOperatorType() {
    return operatorType;
  }

  public void setOperatorType(String operatorType) {
    this.operatorType = operatorType;
  }

  public Long getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Long operatorId) {
    this.operatorId = operatorId;
  }

  public String getPreStage() {
    return preStage;
  }

  public void setPreStage(String preStage) {
    this.preStage = preStage;
  }

  public String getPostStage() {
    return postStage;
  }

  public void setPostStage(String postStage) {
    this.postStage = postStage;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Long id() {
    return id;
  }

  public Long orderId() {
    return orderId;
  }

  public String operatorType() {
    return operatorType;
  }

  public Long operatorId() {
    return operatorId;
  }

  public String preStage() {
    return preStage;
  }

  public String postStage() {
    return postStage;
  }

  public String remark() {
    return remark;
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  @Override
  public StatusLog withId(Long id) {
    return new StatusLog(id, orderId, operatorType, operatorId, preStage, postStage, remark, createdAt);
  }
}
