package com.qhr.dto;

/**
 * 提交测额后后台异步匹配上下文。
 */
public record MeasureAsyncMatchCommand(
        //用户标识
        String openid,
        //企业ID
        Long enterpriseId,
        //融资需求ID
        Long intentionId,
        //工商帐号
        String taxAccount,
        //工商密码
        String taxPassword,
        //验证码
        String verifyCode,
        //企查查模糊查询对象
        EnterprisePayload enterprise
) {
}
