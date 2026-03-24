package com.qhr.config.qcc;

public class QccClientException extends RuntimeException {

  public QccClientException(String message) {
    super(message);
  }

  public QccClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
