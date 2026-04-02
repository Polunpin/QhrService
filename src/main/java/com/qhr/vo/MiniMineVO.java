package com.qhr.vo;

/**
 * 小程序我的。
 */
public record MiniMineVO(
        /*企业主体数量*/
        long enterpriseCount,
        /*测额记录*/
        long measureCount,
        /*提额进度*/
        long increaseProgress,
        /*订单*/
        long orderCount) {
}
