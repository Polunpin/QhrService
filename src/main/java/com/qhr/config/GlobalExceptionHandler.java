package com.qhr.config;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

  private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

  @Override
  public Response toResponse(Throwable throwable) {
    ApiResponse body;
    if (throwable instanceof ApiException ex) {
      LOG.warnf("业务异常 code=%s, message=%s", ex.getCode(), ex.getMessage());
      body = ApiResponse.error(ex.getCode(), ex.getMessage());
    } else if (throwable instanceof IllegalArgumentException ex) {
      LOG.warn("非法参数异常", ex);
      body = ApiResponse.error(ApiCode.BAD_REQUEST, ex.getMessage());
    } else {
      LOG.error("未处理异常", throwable);
      body = ApiResponse.error(ApiCode.INTERNAL_ERROR, "服务器异常");
    }
    // 保持历史接口行为：业务错误也返回200，通过code字段区分
    return Response.ok(body).build();
  }
}
