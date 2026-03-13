package com.qhr.model;

/**
 * 标准化ID替换方法。
 */
public interface WithId<T> {

  T withId(Long id);
}
