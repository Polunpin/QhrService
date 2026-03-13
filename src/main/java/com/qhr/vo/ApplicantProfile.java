package com.qhr.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 申请人画像（统一风控输入对象）
 * 用于信贷产品规则引擎输入
 */
@Data
public class ApplicantProfile {

    /*企业画像*/
    // 企业名称
    private String companyName;

    // 注册地
    private String registerAddress;

    // 成立时间
    private LocalDate establishDate;

    // 行业
    private String industry;

    // 法人持股比例 (%)
    private BigDecimal legalPersonShareRatio;

    // 法人变更次数
    private Integer legalPersonChangeCount;

    // 企业标签（高新企业/专精特新/科技型企业等）
    private String enterpriseTags;

    // 财报数据
    private FinancialReport financialReport;

    @Data
    public static class FinancialReport {

        // 年营业收入
        private BigDecimal annualRevenue;

        // 年利润
        private BigDecimal annualProfit;

        // 总资产
        private BigDecimal totalAssets;

        // 总负债
        private BigDecimal totalLiabilities;

    }
    /*经营画像*/
    // 年开票金额
    private BigDecimal annualInvoiceAmount;

    // 月开票分布
    private List<BigDecimal> monthlyInvoiceAmounts;

    // 年纳税金额
    private BigDecimal annualTaxAmount;

    // 纳税月份
    private List<Integer> taxMonths;

    // 税负率 (%)
    private BigDecimal taxRate;

    // 利润率 (%)
    private BigDecimal profitRate;


    /*个人画像*/
    // 年龄
    private Integer age;

    // 婚姻状况（未婚/已婚/离异）
    private String maritalStatus;

    // 配偶是否连带担保
    private Boolean spouseGuarantee;

    // 征信查询次数（近6个月）
    private Integer creditInquiryCount;

    // 是否存在逾期
    private Boolean hasOverdue;

    // 信用卡使用率 (%)
    private BigDecimal creditCardUsageRate;

    // 贷款机构数
    private Integer loanInstitutionCount;


    /*负债画像*/
    // 企业负债总额
    private BigDecimal enterpriseLiability;

    // 企业信贷余额
    private BigDecimal enterpriseCreditLoan;

    // 个人负债总额
    private BigDecimal personalLiability;

    // 信用贷余额
    private BigDecimal creditLoan;

    // 抵押贷余额
    private BigDecimal mortgageLoan;

    // 担保敞口
    private BigDecimal guaranteeExposure;

    /*衍生指标*/
    // 企业成立年限
    private Integer companyAge;

    // 开票增长率
    private BigDecimal invoiceGrowthRate;

    // 税贷额度预估
    private BigDecimal taxLoanEstimate;

    // 资产负债率
    private BigDecimal debtRatio;
}