package com.qhr.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业实体。
 */
public record Enterprise(Long id,
                         /*企业名称*/
                         String name,
                         /*统一社会信用代码*/
                         String creditCode,
                         /*成立日期*/
                         String startDate,
                         /*法定代表人姓名*/
                         String operName,
                         /*状态*/
                         String status,
                         /*注册地址*/
                         String address,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) implements Serializable, WithId<Enterprise> {

  /** 复制并替换id。 */
  public Enterprise withId(Long id) {
    return new Enterprise(id, name, creditCode, startDate, operName, status, address, createdAt, updatedAt);
  }
}
