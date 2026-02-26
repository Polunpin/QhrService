package com.tencent.vo;

/**
 * 信贷列表。
 */
public record CreditProducts(
        /*信贷产品ID*/
        String id,
        /*银行/机构名称*/
        String bankName,
        /*产品名称*/
        String productName,
        /*产品种类*/
        String productType,
//        /*最低额度(万元)*/
        String minAmount,
        /*最高额度(万元)*/
        String maxAmount,
        /*利率范围(如: 3.5%-5%)*/
        String interestRateRange,
        /*最长期限(月)*/
        String loanTerm,
        /*还款方式*/
        String repaymentMethod,
        /*准入地区 todo 未来地区功能扩展*/
        String region,
        /*准入条件结构化描述*/
        String criteriaJson,
        /*0:下架, 1:上架, 2:草稿*/
        String status,
        /*历史匹配成功率*/
        String successRate) {
}
