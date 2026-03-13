package com.qhr.config;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回结构。
 */
public record PageResult<T>(List<T> items, long total, int page, int size) {

  public static <T> PageResult<T> of(List<T> items, long total, int page, int size) {
    return new PageResult<>(items == null ? Collections.emptyList() : items, total, page, size);
  }
}
