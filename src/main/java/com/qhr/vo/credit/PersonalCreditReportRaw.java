package com.qhr.vo.credit;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 个人征信 PDF 解析后的原始结构化结果。
 * 这一层尽量贴近征信报告原文，避免过早映射成产品匹配字段。
 */
@Data
public class PersonalCreditReportRaw {

    // 报告类型，当前固定为个人征信。
    private String reportType = "PERSONAL_CREDIT";

    // PDF 总页数，用于调试和模板识别。
    private Integer pageCount;

    // 报告编号。
    private String reportNumber;

    // 报告生成时间。
    private String reportTime;

    // 报告主体基础信息。
    private BasicInfo person = new BasicInfo();

    // 从明细回填出的摘要统计。
    private Summary summary = new Summary();

    // 是否存在非信贷交易记录。
    private boolean hasNonCreditRecords;

    // 是否存在公共记录。
    private boolean hasPublicRecords;

    // 信用卡账户明细。
    private List<CreditCardAccount> creditCards = new ArrayList<>();

    // 贷款账户明细。
    private List<LoanAccount> loans = new ArrayList<>();

    // 为企业担保或共同借款形成的相关还款责任。
    private List<RelatedRepaymentLiability> relatedLiabilities = new ArrayList<>();

    // 机构查询记录。
    private List<QueryRecord> institutionQueries = new ArrayList<>();

    // 本人查询记录。
    private List<QueryRecord> selfQueries = new ArrayList<>();

    // 解析过程中未完全识别的内容告警。
    private List<String> parseWarnings = new ArrayList<>();

    /**
     * 报告头部个人基础身份信息。
     */
    @Data
    public static class BasicInfo {

        // 姓名。
        private String name;

        // 证件类型，如身份证。
        private String idType;

        // 证件号码。
        private String idNo;

        // 婚姻状态。
        private String maritalStatus;
    }

    /**
     * 从原始明细汇总出的统计信息，供画像层快速消费。
     */
    @Data
    public static class Summary {

        // 信用卡账户总数。
        private Integer creditCardAccountCount;

        // 未销户且非关闭状态的信用卡账户数。
        private Integer activeCreditCardCount;

        // 发生过逾期的信用卡账户数。
        private Integer overdueCreditCardAccountCount;

        // 发生过 90 天以上逾期的信用卡账户数。
        private Integer overdue90PlusCreditCardAccountCount;

        // 贷款账户总数。
        private Integer loanAccountCount;

        // 未结清贷款账户数。
        private Integer activeLoanCount;

        // 相关还款责任笔数。
        private Integer relatedRepaymentLiabilityCount;

        // 机构查询记录条数。
        private Integer institutionQueryCount;

        // 本人查询记录条数。
        private Integer selfQueryCount;
    }

    /**
     * 单张信用卡账户记录。
     */
    @Data
    public static class CreditCardAccount {

        // 发卡日期。
        private String issueDate;

        // 发卡机构。
        private String institution;

        // 卡类型，如贷记卡、准贷记卡。
        private String cardType;

        // 账户币种或账户类型描述。
        private String accountCurrency;

        // 卡号尾号。
        private String cardTailNumber;

        // 账户状态，如 OPEN、CLOSED、NOT_ACTIVATED。
        private String accountStatus;

        // 状态对应月份，例如销户月份、未激活截至月份。
        private String statusMonth;

        // 报告统计截止月份。
        private String asOfMonth;

        // 信用额度。
        private BigDecimal creditLimit;

        // 当前余额。
        private BigDecimal balance;

        // 当前是否逾期。
        private Boolean currentOverdue;

        // 最近 5 年逾期月数。
        private Integer overdueMonthsInLast5Years;

        // 是否出现过 90 天以上逾期。
        private Boolean hasOverdue90Plus;
    }

    /**
     * 单笔贷款账户记录。
     */
    @Data
    public static class LoanAccount {

        // 发放或授信日期。
        private String issueDate;

        // 放款机构。
        private String institution;

        // 贷款类型，如个人经营性贷款、个人消费贷款。
        private String loanType;

        // 账户模式，如 REVOLVING、DISBURSED。
        private String accountMode;

        // 报告统计截止月份。
        private String asOfMonth;

        // 币种。
        private String currency;

        // 授信有效期截止日。
        private String limitValidUntil;

        // 是否循环使用。
        private Boolean revolving;

        // 已结清月份。
        private String settledMonth;

        // 循环授信额度。
        private BigDecimal creditLimit;

        // 已发放金额。
        private BigDecimal amountIssued;

        // 当前余额。
        private BigDecimal balance;

        // 当前是否逾期。
        private Boolean currentOverdue;

        // 当前状态，如 NORMAL、SETTLED、OVERDUE。
        private String currentStatus;
    }

    /**
     * 相关还款责任记录，主要用于识别保证人和共同借款人负债。
     */
    @Data
    public static class RelatedRepaymentLiability {

        // 责任形成日期。
        private String issueDate;

        // 关联企业名称。
        private String companyName;

        // 企业证件类型。
        private String companyIdType;

        // 企业证件号码或中征码。
        private String companyIdNo;

        // 办理贷款的机构。
        private String institution;

        // 责任人类型，如保证人、共同借款人。
        private String liabilityType;

        // 相关还款责任金额。
        private BigDecimal liabilityAmount;

        // 余额统计日期。
        private String asOfDate;

        // 截至统计日的贷款余额。
        private BigDecimal loanBalance;
    }

    /**
     * 单条查询记录。
     */
    @Data
    public static class QueryRecord {

        // 报告中的序号。
        private Integer index;

        // 查询日期。
        private String queryDate;

        // 查询机构。
        private String institution;

        // 查询原因。
        private String queryReason;
    }
}
