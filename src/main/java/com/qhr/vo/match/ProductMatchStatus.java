package com.qhr.vo.match;

/**
 * 产品匹配结果状态。
 */
public enum ProductMatchStatus {
    /**
     * 明确命中，可直接进入候选产品池
     */
    MATCH,
    /** 可做，但需要补件、确认申请动作或人工复核 */
    REVIEW,
    /** 明确不满足规则，直接拒绝 */
    REJECT,
    /** 因关键字段缺失，当前无法完成判断 */
    INSUFFICIENT_DATA
}
