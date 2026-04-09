package com.qhr.dto;

/**
 * 企查查财税数据请求。
 * taxData 会依次调用下单、验证码发送、数据获取三个接口。
 */
public record QccTaxCreateOrderRequest(
        //搜索关键词（统一社会信用代码、企业名称）
        String searchKey,
        //税务局用户名（SM4加密，密钥为用户key）
        String userName,
        //税务局密码（SM4加密，密钥为用户key）
        String password,
        //qcc订单号
        String orderNo,
        //验证码
        String verifyCode
) {
}



