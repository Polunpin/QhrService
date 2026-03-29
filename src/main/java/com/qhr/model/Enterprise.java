package com.qhr.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业实体。
 */
@TableName("jc_enterprise_basic_info")
public class Enterprise implements Serializable, WithId<Enterprise> {

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

  public Enterprise() {
  }

  public Enterprise(Long id, String name, String creditCode, String startDate, String operName, String status,
                    String address, LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.name = name;
    this.creditCode = creditCode;
    this.startDate = startDate;
    this.operName = operName;
    this.status = status;
    this.address = address;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCreditCode() {
    return creditCode;
  }

  public void setCreditCode(String creditCode) {
    this.creditCode = creditCode;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getOperName() {
    return operName;
  }

  public void setOperName(String operName) {
    this.operName = operName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
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

  public String name() {
    return name;
  }

  public String creditCode() {
    return creditCode;
  }

  public String startDate() {
    return startDate;
  }

  public String operName() {
    return operName;
  }

  public String status() {
    return status;
  }

  public String address() {
    return address;
  }

  public LocalDateTime createdAt() {
    return createdAt;
  }

  public LocalDateTime updatedAt() {
    return updatedAt;
  }

  @Override
  public Enterprise withId(Long id) {
    return new Enterprise(id, name, creditCode, startDate, operName, status, address, createdAt, updatedAt);
  }
}
