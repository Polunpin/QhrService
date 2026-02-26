package com.tencent.vo;

/**
 * 信贷产品统计
 */
public record CreditProductStats(
        //合作机构
        String bankNameCount,
        //上架产品
        String productCount,
        //本周匹配
        String matchCount,
        //本周匹配成功率
        String matchRate) {
}
