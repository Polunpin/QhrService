package com.qhr.vo.match;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 额度策略 payload。
 */
@Data
public class AmountStrategyPayload {

    /**
     * 策略类型，如 FORMULA、MANUAL_REVIEW
     */
    private String strategyType;
    /**
     * 额度公式 key，用于路由到实际额度计算器
     */
    private String formulaKey;
    /**
     * 公式描述或表达式文本
     */
    private String formulaExpr;
    /**
     * 额度单位，如 WAN
     */
    private String amountUnit;
    /**
     * 额度上限
     */
    private BigDecimal ceiling;
    /**
     * 额度下限
     */
    private BigDecimal floor;
    /**
     * 当结果为负值或异常时是否转人工审核
     */
    private Boolean manualReviewWhenNegative;
}
