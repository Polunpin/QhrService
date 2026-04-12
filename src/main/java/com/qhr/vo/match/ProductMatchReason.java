package com.qhr.vo.match;

import lombok.Data;

/**
 * 结构化匹配原因，用于前端展示和后续提额诊断复用。
 */
@Data
public class ProductMatchReason {

    /**
     * 对应产品ID；用于在汇总后仍能定位这条原因属于哪个产品
     */
    private Long productId;
    /**
     * 原因类型，通常为 REVIEW / REJECT / INSUFFICIENT_DATA
     */
    private ProductMatchStatus reasonType;
    /**
     * 原因编码，便于前后端和诊断规则做稳定映射
     */
    private String code;
    /** 面向展示的原因说明 */
    private String message;
    /** 产生命中结果的决策节点，如 BasicGate、CreditGate、TaxGate、ContextGate */
    private String decisionNode;
    /** 对应的画像字段或上下文字段名 */
    private String sourceField;
    /** 规则期望值 */
    private Object expected;
    /** 当前实际值 */
    private Object actual;
    /** 是否为硬拒绝原因；true 表示会直接进入 REJECT */
    private Boolean hardReject;
}
