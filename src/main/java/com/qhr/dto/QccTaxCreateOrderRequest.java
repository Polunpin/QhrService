package com.qhr.dto;

/**
 * 企查查财税数据请求。
 * taxData 会依次调用下单、验证码发送、数据获取三个接口。
 */
public record QccTaxCreateOrderRequest(
        String searchKey,
        String userName,
        String password,
        String verifyCode
) {
}
