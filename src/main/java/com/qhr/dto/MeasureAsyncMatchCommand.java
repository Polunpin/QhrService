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
        //qcc财税数据订单号
        String orderNo,
        //验证码
        String verifyCode,
        //数据下单状态，判断是否需要短信验证
        String dataStatus
) {
}
