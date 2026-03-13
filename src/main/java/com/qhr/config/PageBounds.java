package com.qhr.config;

/**
 * 分页边界数据，提前计算 offset 以减少重复计算。
 */
public record PageBounds(int page, int size, int offset) {

  public static PageBounds of(Integer page, Integer size) {
    int safePage = PageRequest.normalizePage(page);
    int safeSize = PageRequest.normalizeSize(size);
    return new PageBounds(safePage, safeSize, PageRequest.offset(safePage, safeSize));
  }
}
