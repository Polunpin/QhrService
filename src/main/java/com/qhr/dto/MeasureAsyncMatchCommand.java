package com.qhr.dto;

/**
 * 提交测额后后台异步匹配上下文。
 */
public record MeasureAsyncMatchCommand(
        String openid,
        Long enterpriseId,
        Long intentionId,
        Boolean spouseSupport,
        String taxAccount,
        String taxPassword,
        String verifyCode,
        EnterprisePayload enterprise
) {
}
