package com.qhr.vo;

/**
 * 小程序首页融资驾驶舱。
 */
public record MiniHomeDashboardVO(
        /*可贷额度区间*/
        String amountRange,
        //雷达图
        MiniHomeRadarVO radar,
        /*匹配产品数量*/
        Integer matchedProductCount,
        /*最低年华*/
        String minimumAnnualRate) {
}
