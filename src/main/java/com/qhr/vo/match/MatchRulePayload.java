package com.qhr.vo.match;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 详细匹配规则 payload。
 */
@Data
public class MatchRulePayload {

    /**
     * 税票、报表相关精判规则
     */
    private TaxRule tax = new TaxRule();
    /**
     * 企业征信精判规则
     */
    private EnterpriseCreditRule enterpriseCredit = new EnterpriseCreditRule();
    /**
     * 个人征信精判规则
     */
    private PersonalCreditRule personalCredit = new PersonalCreditRule();
    /**
     * REVIEW 型规则，命中后进入补件/人工确认，而不是直接拒绝
     */
    private List<ReviewRule> reviewRules = new ArrayList<>();

    @Data
    public static class TaxRule {
        /**
         * 近12个月最低开票金额
         */
        private BigDecimal minInvoiceAmount12m;
        /**
         * 当年最低开票金额
         */
        private BigDecimal minInvoiceAmountYtd;
        /**
         * 上一自然年最低开票金额
         */
        private BigDecimal minInvoiceAmountLastYear;
        /**
         * 近12个月最低开票月数
         */
        private Integer minInvoiceMonths12m;
        /**
         * 近12个月最少纳税月份数
         */
        private Integer minTaxMonths12m;
        /**
         * 近24个月最少纳税月份数
         */
        private Integer minTaxMonths24m;
        /**
         * 近12个月最低纳税金额
         */
        private BigDecimal minTaxAmount12m;
        /**
         * 当年最低纳税金额
         */
        private BigDecimal minTaxAmountYtd;
        /**
         * 上一自然年最低纳税金额
         */
        private BigDecimal minTaxAmountLastYear;
        /**
         * 税负率上限
         */
        private BigDecimal maxTaxBurdenRatio;
        /**
         * 税负率同比下限，如当年/上年
         */
        private BigDecimal minTaxBurdenRatioYoy;
        /**
         * 累计纳税申报次数下限
         */
        private Integer minTotalDeclares;
        /**
         * 是否接受内账资料
         */
        private Boolean allowInternalAccounts;
        /**
         * 最近完整自然年财报资产负债率上限
         */
        private BigDecimal maxLastFullYearDebtRatio;
        /**
         * 最新财报资产负债率上限
         */
        private BigDecimal maxLatestDebtRatio;
    }

    @Data
    public static class EnterpriseCreditRule {
        /**
         * 企业总负债上限
         */
        private BigDecimal maxTotalLiability;
        /**
         * 企业信贷负债上限
         */
        private BigDecimal maxCreditLiability;
        /**
         * 企业抵押负债上限
         */
        private BigDecimal maxMortgageLiability;
        /**
         * 企业对外担保余额上限
         */
        private BigDecimal maxExternalGuaranteeAmount;
        /**
         * 企业贷款机构数上限
         */
        private Integer maxLoanOrgCount;
        /**
         * 近6个月企业征信查询次数上限
         */
        private Integer maxQueryCount6m;
        /**
         * 近12个月企业逾期次数上限
         */
        private Integer maxOverdueCount12m;
        /**
         * 近24个月企业逾期次数上限
         */
        private Integer maxOverdueCount24m;
        /**
         * 近24个月企业最大逾期月数上限
         */
        private Integer maxOverdueMonths24m;
        /**
         * 企业最短无逾期天数下限
         */
        private Integer minNoOverdueDays;
        /**
         * 是否允许企业征信账户状态异常
         */
        private Boolean allowAbnormalCreditAccount;
    }

    @Data
    public static class PersonalCreditRule {
        /**
         * 近1个月个人查询次数上限
         */
        private Integer maxQueryCount1m;
        /**
         * 近3个月个人查询次数上限
         */
        private Integer maxQueryCount3m;
        /**
         * 近6个月个人查询次数上限
         */
        private Integer maxQueryCount6m;
        /**
         * 近12个月个人查询次数上限
         */
        private Integer maxQueryCount12m;
        /**
         * 近1个月贷款审批查询次数上限
         */
        private Integer maxLoanQueryCount1m;
        /**
         * 近1个月信用卡审批查询次数上限
         */
        private Integer maxCreditCardQueryCount1m;
        /**
         * 近12个月个人逾期次数上限
         */
        private Integer maxOverdueCount12m;
        /**
         * 近24个月个人逾期次数上限
         */
        private Integer maxOverdueCount24m;
        /**
         * 近24个月个人最大逾期月数上限
         */
        private Integer maxOverdueMonths24m;
        /**
         * 近5年累计逾期期数上限
         */
        private Integer maxTotalOverdueTerms5y;
        /**
         * 非银行网贷/小贷笔数上限
         */
        private Integer maxNonBankLoanCount;
        /**
         * 当前信用卡使用率上限
         */
        private BigDecimal maxCreditCardUtilization;
        /**
         * 是否允许个人征信账户状态异常
         */
        private Boolean allowAbnormalCreditAccount;
    }

    @Data
    public static class ReviewRule {
        /**
         * REVIEW 规则编码
         */
        private String code;
        /**
         * 对应的上下文或画像字段
         */
        private String field;
        /**
         * 期望值，用于前端和解释层展示
         */
        private Object expectedValue;
        /**
         * REVIEW 提示语
         */
        private String message;
    }
}
