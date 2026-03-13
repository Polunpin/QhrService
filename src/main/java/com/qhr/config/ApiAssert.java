package com.qhr.config;

/**
 * 统一断言入口，保持错误码策略一致。
 */
public final class ApiAssert {

  private ApiAssert() {
  }

  public static void notNull(Object target, int code, String message) {
    if (target == null) {
      throw new ApiException(code, message);
    }
  }

  public static void isTrue(boolean condition, int code, String message) {
    if (!condition) {
      throw new ApiException(code, message);
    }
  }
}
