package com.tencent.config;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable throwable) {
    ApiResponse body;
    if (throwable instanceof ApiException ex) {
      body = ApiResponse.error(ex.getCode(), ex.getMessage());
    } else if (throwable instanceof IllegalArgumentException ex) {
      body = ApiResponse.error(ApiCode.BAD_REQUEST, ex.getMessage());
    } else {
      body = ApiResponse.error(ApiCode.INTERNAL_ERROR, "服务器异常");
    }
    // 保持历史接口行为：业务错误也返回200，通过code字段区分
    return Response.ok(body).build();
  }
}
