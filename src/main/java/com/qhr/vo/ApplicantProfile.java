package com.qhr.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 申请人画像（统一风控输入对象）。
 * 画像层只存标准化后的业务事实，不直接存原始三方报文。
 */
@Data
public class ApplicantProfile {

    /*企业基础*/
    private String companyName;
    private String unifiedSocialCreditCode;
    private String registerAddress;
    private String companyRegion;
    private LocalDate establishDate;
    private Integer companyAge;
    private Integer companyAgeMonths;
    private String companyStatus;
    private String industry;
    private List<String> industryTags;
    private BigDecimal registeredCapital;
    private BigDecimal paidInCapital;

    /*主体关系*/
    private BigDecimal legalPersonShareRatio;
    private Integer legalPersonChangeCount;
    private Integer legalPersonChangeCount2y;
    private Integer legalPersonLastChangeGapMonths;
    private String enterpriseTags;
    private List<Integer> enterpriseTagCodes;
    private Integer enterpriseTagExpireMonths;
    private String taxLevel;
    private Boolean legalRepCanJointLiability;
    private Boolean shareholderCanJointLiability;
    private BigDecimal shareholderJointLiabilityShareRatio;
    private Boolean spouseCanJointLiability;

    /*财报*/
    private FinancialReport financialReport;

    @Data
    public static class FinancialReport {
        private BigDecimal annualRevenue;
        private BigDecimal annualProfit;
        private BigDecimal totalAssets;
        private BigDecimal totalLiabilities;
    }

    /*开票经营*/
    private BigDecimal annualInvoiceAmount;
    private List<BigDecimal> monthlyInvoiceAmounts;
    private BigDecimal invoiceAmount12m;
    private BigDecimal invoiceAmountCurrentYear;
    private BigDecimal invoiceAmountLastYear;
    private BigDecimal invoiceAmount2025;
    private BigDecimal invoiceAmount2024;
    private BigDecimal invoiceAmount2023;
    private BigDecimal invoiceAmount2022;
    private Integer invoiceMonths12m;
    private BigDecimal invoiceGrowthRate;
    private BigDecimal invoiceRatioLast2FullYears;

    /*纳税经营*/
    private BigDecimal annualTaxAmount;
    private List<Integer> taxMonths;
    private Integer taxMonths12m;
    private Integer taxMonths24m;
    private Integer zeroDeclareMaxConsecutive12m;
    private Integer zeroDeclareMaxConsecutive24m;
    private BigDecimal taxAmount12m;
    private BigDecimal taxAmountCurrentYear;
    private BigDecimal taxAmountLastYear;
    private BigDecimal taxAmount2025;
    private BigDecimal taxAmount2024;
    private BigDecimal taxAmount2023;
    private BigDecimal taxAmount2022;
    private BigDecimal taxRate;
    private BigDecimal taxBurdenRate1y;
    private BigDecimal taxBurdenRate2025;
    private BigDecimal taxBurdenRate2024;
    private BigDecimal taxBurdenRateRatio25vs24;
    private Integer totalTaxDeclarationCount;

    /*利润与资产负债率*/
    private BigDecimal profitRate;
    private BigDecimal profit1y;
    private BigDecimal profitCurrentYear;
    private BigDecimal profitLastFullYear;
    private BigDecimal profit2025;
    private BigDecimal profit2024;
    private BigDecimal profit2023;
    private BigDecimal debtRatio;
    private BigDecimal lastFullYearDebtRatio;
    private BigDecimal latestDebtRatio;
    private Boolean canProvideInternalAccounts;

    /*个人画像*/
    private Integer age;
    private String maritalStatus;
    private Integer creditInquiryCount;
    private Boolean hasOverdue;
    private BigDecimal creditCardUsageRate;
    private Integer loanInstitutionCount;
    private Integer reviewQueryCount2w;
    private Integer reviewQueryCount1m;
    private Integer reviewQueryCount2m;
    private Integer reviewQueryCount3m;
    private Integer reviewQueryCount6m;
    private Integer reviewQueryCount12m;
    private Integer loanOrCardQueryCount1m;
    private Integer loanOrCardQueryCount12m;
    private Integer loanQueryCount1m;
    private Integer loanQueryCount4m;
    private Integer creditCardQueryCount1m;
    private Integer overdueCount12m;
    private Integer maxOverdueMonths12m;
    private Integer overdueCount24m;
    private Integer maxOverdueMonths24m;
    private Integer overdueTerms5y;
    private Integer maxConsecutiveOverdueTerms5y;
    private Integer daysSinceLastOverdue;
    private String bankOverdueStatus;
    private Boolean abnormalCreditAccount;

    /*企业征信占位*/
    private Integer enterpriseLoanInstitutionCount;
    private Integer enterpriseQueryCount6m;
    private Integer enterpriseOverdueCount12m;
    private Integer enterpriseMaxOverdueMonths24m;
    private Integer enterpriseNoOverdueDays;
    private Boolean enterpriseAbnormalCreditAccount;

    /*负债画像*/
    private BigDecimal enterpriseLiability;
    private BigDecimal enterpriseCreditLoan;
    private BigDecimal enterpriseMortgageLiability;
    private BigDecimal enterpriseGuaranteeLiability;
    private BigDecimal enterpriseDebtToLastYearInvoiceRatio;
    private BigDecimal personalLiability;
    private BigDecimal personalLiabilityExcludingGuarantee;
    private BigDecimal personalLiabilityIncludingGuarantee;
    private BigDecimal creditLoan;
    private BigDecimal mortgageLoan;
    private BigDecimal guaranteeExposure;
    private BigDecimal combinedDebtTotalInclGuaranteeCard;
    private BigDecimal combinedCreditLoanDebt;
    private BigDecimal combinedMortgageDebt;
    private BigDecimal personalCreditLoanDebt;
    private BigDecimal combinedDebtToLastYearInvoiceRatio;
    private BigDecimal personalDebtToLastYearInvoiceRatio;
    private Integer personalLoanInstitutionCount;
    private Integer personalConsumerLoanCount;
    private Integer personalOnlineSmallLoanCount;
    private Integer personalUnsecuredLoanInstitutionCount;
    private Integer creditCardCount;
    private BigDecimal creditCardUtilizationCurrent;
    private BigDecimal creditCardUtilizationLastMonth;

    /*稳定性补充*/
    private Integer realNamePhoneMonths;
    private Integer providentFundContributionMonths;

    /*兼容旧接口保留*/
    private BigDecimal taxLoanEstimate;
}
