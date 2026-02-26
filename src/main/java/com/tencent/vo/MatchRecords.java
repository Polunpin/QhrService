package com.tencent.vo;

import lombok.Data;

import java.util.List;

/**
 * 匹配记录实体。
 */
@Data
public class MatchRecords {
    private Long id;
    private String enterprise;/*企业名称*/
    private String applicant;/*经办人*/
    private List<Product> products;/*匹配产品-列表*/
    private String riskType;/*风险类型*/
    private String riskLevel;/*风险级别*/
    private String status;/*匹配状态*/
    private String matchTime;/*风险类型*/

    @Data
    public static class Product {
        private String id;
        private String loanTerm;
        private String maxAmount;
        private String productName;
        private String interestRateRange;
    }

}
