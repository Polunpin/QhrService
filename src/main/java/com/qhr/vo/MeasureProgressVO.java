package com.qhr.vo;

/**
 * 测额任务当前进度快照。
 * 字段保持轻量，直接对应前端展示所需的最小结构。
 */
public record MeasureProgressVO(
        String stageKey,
        int stageIndex,
        int stageTotal,
        String title,
        String message,
        String status,
        int progress
) {
}
