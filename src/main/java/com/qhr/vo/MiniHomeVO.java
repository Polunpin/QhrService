package com.qhr.vo;

/**
 * 小程序首页。
 */
public record MiniHomeVO(
        /*首页融资驾驶舱*/
        MiniHomeDashboardVO dashboard,
        /*首页额度提升预测*/
        MiniHomeQuotaPredictionVO quotaPrediction
) {
}
