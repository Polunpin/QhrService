package com.qhr.vo;

import java.time.LocalDateTime;

/**
 * 小程序提额查询项。
 */
public record MiniMeasureItemVO(
        /*企业ID*/
        long enterpriseId,
        /*企业名称*/
        long enterpriseName,
        /*可贷额度区间*/
        long amountRange,
        /*可贷额度区间*/
        String matchStatus,
        /*可贷额度区间*/
        String riskLevel,
        /*可贷额度区间*/
        LocalDateTime createdAt) {
}
