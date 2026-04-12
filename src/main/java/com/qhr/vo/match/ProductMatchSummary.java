package com.qhr.vo.match;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量产品匹配汇总。
 * 对外只返回命中产品ID以及两类结构化原因，不再返回逐产品展示字段。
 */
@Data
public class ProductMatchSummary {

    /**
     * 明确命中的产品ID列表，仅包含状态为 MATCH 的产品
     */
    private List<Long> productIds = new ArrayList<>();
    /**
     * 可做但需补件、确认申请动作或人工复核的结构化原因
     */
    private List<ProductMatchReason> reviewReasons = new ArrayList<>();
    /**
     * 拒绝及数据缺失原因，用于回溯、诊断和后续规则治理
     */
    private List<ProductMatchReason> rejectReasons = new ArrayList<>();
}
