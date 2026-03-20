package com.qhr.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体。
 */
public record User(Long id,
                   String openid,
                   String unionid,
                   String mobile,
                   String realName,
                   Integer status,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) implements Serializable, WithId<User> {

  /** 复制并替换id。 */
  public User withId(Long id) {
    return new User(id, openid, unionid, mobile, realName, status, createdAt, updatedAt);
  }
}
