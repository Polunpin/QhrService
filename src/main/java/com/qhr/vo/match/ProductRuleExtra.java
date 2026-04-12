package com.qhr.vo.match;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ProductRule.ext_json 的结构化承载对象。
 * 用于承接当前 yw_product_rule 主表还未覆盖的 Excel 规则字段。
 */
@Data
public class ProductRuleExtra {

    private List<String> allowedRegions;
    private List<String> requiredIndustryKeywords;
    private String industryRestrictionDescription;
    private Integer minAge;
    private Integer maxAge;
    private Integer maxCombinedLoanInstitutionRank;
    private Integer maxEnterpriseLoanInstitutionRank;
    private Integer maxEnterpriseCreditInstitutionCount;
    private List<String> restrictedIndustries;
    private String applyChannel;
    private String disbursementAccountType;
    private Boolean requireContractInvoiceReceipt;
    private Boolean requireOnsiteInvestigation;
    private Map<String, BigDecimal> minInvoiceAmountByYear;
    private BigDecimal minInvoiceAmount12m;
    private BigDecimal minInvoiceAmountCurrentYear;
    private BigDecimal minInvoiceRatioLast2FullYears;
    private Integer minInvoiceMonths12m;
    private Integer maxZeroDeclareConsecutive12m;
    private Integer maxZeroDeclareConsecutive24m;
    private Map<String, BigDecimal> minTaxAmountByYear;
    private BigDecimal minProfit1y;
    private BigDecimal minProfitCurrentYear;
    private Map<String, BigDecimal> minProfitByYear;
    private BigDecimal maxLastFullYearDebtRatio;
    private BigDecimal maxLatestDebtRatio;
    private Boolean acceptInternalAccounts;
    private Integer maxReviewQueryCount2w;
    private Integer maxReviewQueryCount2m;
    private Integer maxReviewQueryCount12m;
    private Integer maxLoanOrCardQueryCount12m;
    private Integer maxLoanQueryCount4m;
    private Integer maxOverdueCount24m;
    private Integer maxOverdueMonths12m;
    private Integer maxOverdueTerms5y;
    private Integer maxConsecutiveOverdueTerms5y;
    private String bankOverdueRequirement;
    private Boolean allowAbnormalCreditAccount;
    private Boolean requireSpouseJointLiability;
    private BigDecimal maxCombinedDebtTotalInclGuaranteeCard;
    private BigDecimal maxCombinedCreditLoanDebt;
    private BigDecimal maxCombinedMortgageDebt;
    private BigDecimal maxEnterpriseDebtToInvoiceRatio;
    private BigDecimal maxCombinedDebtToInvoiceRatio;
    private BigDecimal maxPersonalDebtToInvoiceRatio;
    private BigDecimal maxPersonalDebtExcludingGuarantee;
    private BigDecimal maxPersonalDebtIncludingGuarantee;
    private BigDecimal maxPersonalCreditLoanDebt;
    private Integer maxPersonalLoanInstitutionCount;
    private Integer maxPersonalConsumerLoanCount;
    private Integer maxPersonalOnlineSmallLoanCount;
    private Integer maxPersonalUnsecuredLoanInstitutionCount;
    private Integer maxCreditCardCount;
    private BigDecimal maxCreditCardUtilizationCurrent;
    private BigDecimal maxCreditCardUtilizationLastMonth;
    private Integer minRealNamePhoneMonths;
    private Integer minProvidentFundContributionMonths;
}
