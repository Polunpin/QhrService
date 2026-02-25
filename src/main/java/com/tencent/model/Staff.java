package com.tencent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内部员工实体。
 */
public record Staff(Long id,
                    String openid,
                    @JsonProperty("name")
                    String realName,
                    @JsonProperty("phone")
                    String mobile,
                    String role,
                    String department,
                    Integer status,
                    LocalDateTime lastUpdateAt,
                    LocalDateTime createdAt) implements Serializable, WithId<Staff> {

  /** 复制并替换id。 */
  public Staff withId(Long id) {
    return new Staff(id, openid, realName, mobile, role, department, status, lastUpdateAt, createdAt);
  }
}
