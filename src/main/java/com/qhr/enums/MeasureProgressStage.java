package com.qhr.enums;

import java.util.Arrays;

/**
 * 测额异步处理的阶段定义。
 * 同时约定前端展示文案和步骤序号。
 */
public enum MeasureProgressStage {

    PRECHECK_PASSED("PRECHECK_PASSED", "precheck_passed", 1, "预审通过", "企业基础准入校验已通过，进入方案生成流程。"),
    PROFILE_BUILDING("PROFILE_BUILDING", "build_profile", 2, "生成企业画像", "正在生成企业画像"),
    PRODUCT_MATCHING("PRODUCT_MATCHING", "match_products", 3, "匹配融资产品", "正在筛选通过率更高的融资产品"),
    RECOMMENDATION_GENERATING("RECOMMENDATION_GENERATING", "generate_recommendation", 4, "整理专属建议", "正在整理专属建议"),
    COMPLETED("COMPLETED", "completed", 4, "方案生成完成", "已输出测额结果与建议。"),
    FAILED("FAILED", "failed", 4, "方案生成失败", "处理过程中发生异常，请稍后重试。");

    private final String code;
    private final String stageKey;
    private final int step;
    private final String title;
    private final String description;

    MeasureProgressStage(String code, String stageKey, int step, String title, String description) {
        this.code = code;
        this.stageKey = stageKey;
        this.step = step;
        this.title = title;
        this.description = description;
    }

    /**
     * 按持久化编码反查阶段定义。
     */
    public static MeasureProgressStage fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(stage -> stage.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public String code() {
        return code;
    }

    public String stageKey() {
        return stageKey;
    }

    public int step() {
        return step;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    /**
     * 是否属于前端步骤条展示的主阶段。
     */
    public boolean isDisplayStage() {
        return this == PRECHECK_PASSED
                || this == PROFILE_BUILDING
                || this == PRODUCT_MATCHING
                || this == RECOMMENDATION_GENERATING;
    }
}
