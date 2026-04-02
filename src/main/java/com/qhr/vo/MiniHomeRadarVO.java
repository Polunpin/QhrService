package com.qhr.vo;

/**
 * 小程序首页雷达图。
 */
public record MiniHomeRadarVO(
        /*成立年限*/
        Integer yearScore,
        /*纳税额度*/
        Integer taxScore,
        /*经营流水*/
        Integer operationScore,
        /*现有负债*/
        Integer debtScore,
        /*风险指标*/
        Integer riskScore) {
}