package com.qhr.vo;

/**
 * 小程序首页额度提升预测。
 */
public record MiniHomeQuotaPredictionVO(
        /*预计可提额*/
        String increaseRange,
        /*可优化项*/
        Integer optimizableCount,
        /*预计周期*/
        Integer cycleDays) {
}


