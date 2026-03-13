package com.qhr.config;

public final class PageRequest {

  // 默认分页策略，避免大查询
  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 20;
  private static final int MAX_SIZE = 100;

  private PageRequest() {
  }

  public static int normalizePage(Integer page) {
    if (page == null || page < 1) {
      return DEFAULT_PAGE;
    }
    return page;
  }

  public static int normalizeSize(Integer size) {
    if (size == null || size < 1) {
      return DEFAULT_SIZE;
    }
    return Math.min(size, MAX_SIZE);
  }

  public static int offset(int page, int size) {
    return (page - 1) * size;
  }
}
