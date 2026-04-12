package com.qhr.vo.match;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 提额诊断 payload。
 */
@Data
public class DiagnosisRulePayload {

    /**
     * 可优化项列表，每一项描述一个提额或新增产品的诊断建议
     */
    private List<DiagnosisItem> items = new ArrayList<>();

    @Data
    public static class DiagnosisItem {
        /**
         * 诊断对应的画像或上下文字段
         */
        private String field;
        /**
         * 目标值或目标区间的表达
         */
        private Object targetValue;
        /**
         * 动作编码，供前端或策略层映射执行建议
         */
        private String actionCode;
        /**
         * 影响类型，如 PASS_RATE、AMOUNT、NEW_PRODUCT
         */
        private String impact;
        /**
         * 面向用户或顾问的提示语
         */
        private String message;
    }
}
