package com.qhr.vo.credit;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业信用报告 PDF 解析后的原始结构化结果。
 * 这一层尽量贴近企业征信报告原文，避免过早耦合产品规则字段。
 */
@Data
public class EnterpriseCreditReportRaw {

    /**
     * 报告类型，当前固定为企业征信。
     */
    private String reportType = "ENTERPRISE_CREDIT";
    /**
     * PDF 总页数，用于调试和版式识别。
     */
    private Integer pageCount;
    /**
     * 报告编号。
     */
    private String reportNumber;
    /**
     * 报告生成时间。
     */
    private String reportTime;
    /**
     * 报告主体基础信息。
     */
    private BasicInfo enterprise = new BasicInfo();
    /**
     * 信息概要。
     */
    private Summary summary = new Summary();
    /**
     * 基本概况信息。
     */
    private BasicProfile basicProfile = new BasicProfile();
    /**
     * 未结清贷款明细。
     */
    private List<UnsettledLoan> unsettledLoans = new ArrayList<>();
    /**
     * 已识别的纳税信用等级，如 A/B/M/C/D。
     */
    private List<String> taxCreditLevels = new ArrayList<>();
    /**
     * 解析过程中未完全识别的内容告警。
     */
    private List<String> parseWarnings = new ArrayList<>();

    @Data
    public static class BasicInfo {
        /**
         * 企业名称。
         */
        private String companyName;
        /**
         * 中征码。
         */
        private String centCode;
        /**
         * 统一社会信用代码。
         */
        private String unifiedSocialCreditCode;
        /**
         * 查询机构。
         */
        private String queryInstitution;
    }

    @Data
    public static class Summary {
        /**
         * 首次发生信贷交易年份。
         */
        private Integer firstCreditYear;
        /**
         * 发生信贷交易的机构数。
         */
        private Integer creditInstitutionCount;
        /**
         * 当前有未结清信贷交易的机构数。
         */
        private Integer activeCreditInstitutionCount;
        /**
         * 首次有相关还款责任的年份。
         */
        private Integer firstRelatedRepaymentYear;

        /**
         * 借贷交易余额。
         */
        private BigDecimal loanBalance;
        /**
         * 借贷交易被追偿余额。
         */
        private BigDecimal recoveredLoanBalance;
        /**
         * 借贷交易关注类余额。
         */
        private BigDecimal loanConcernBalance;
        /**
         * 借贷交易不良类余额。
         */
        private BigDecimal loanBadBalance;

        /**
         * 担保交易余额。
         */
        private BigDecimal guaranteeBalance;
        /**
         * 担保交易关注类余额。
         */
        private BigDecimal guaranteeConcernBalance;
        /**
         * 担保交易不良类余额。
         */
        private BigDecimal guaranteeBadBalance;

        /**
         * 非信贷交易账户数。
         */
        private Integer nonCreditAccountCount;
        /**
         * 欠税记录条数。
         */
        private Integer taxArrearsCount;
        /**
         * 民事判决记录条数。
         */
        private Integer civilJudgmentCount;
        /**
         * 强制执行记录条数。
         */
        private Integer enforcementCount;
        /**
         * 行政处罚记录条数。
         */
        private Integer adminPenaltyCount;

        /**
         * 当前短期借款账户数。
         */
        private Integer shortTermLoanAccountCount;
        /**
         * 当前短期借款余额。
         */
        private BigDecimal shortTermLoanBalance;
        /**
         * 当前其他担保交易账户数。
         */
        private Integer otherGuaranteeAccountCount;
        /**
         * 当前其他担保交易余额。
         */
        private BigDecimal otherGuaranteeBalance;

        /**
         * 非循环信用额度总额。
         */
        private BigDecimal nonRevolvingCreditTotal;
        /**
         * 非循环信用额度已用额度。
         */
        private BigDecimal nonRevolvingCreditUsed;
        /**
         * 非循环信用额度剩余可用额度。
         */
        private BigDecimal nonRevolvingCreditAvailable;
        /**
         * 循环信用额度总额。
         */
        private BigDecimal revolvingCreditTotal;
        /**
         * 循环信用额度已用额度。
         */
        private BigDecimal revolvingCreditUsed;
        /**
         * 循环信用额度剩余可用额度。
         */
        private BigDecimal revolvingCreditAvailable;

        /**
         * 相关还款责任金额。
         */
        private BigDecimal relatedRepaymentAmount;
        /**
         * 相关还款责任账户数。
         */
        private Integer relatedRepaymentAccountCount;
        /**
         * 相关还款责任余额。
         */
        private BigDecimal relatedRepaymentBalance;
        /**
         * 相关还款责任关注类余额。
         */
        private BigDecimal relatedRepaymentConcernBalance;
        /**
         * 相关还款责任不良类余额。
         */
        private BigDecimal relatedRepaymentBadBalance;
    }

    @Data
    public static class BasicProfile {
        /**
         * 经济类型。
         */
        private String economicType;
        /**
         * 组织机构类型。
         */
        private String organizationType;
        /**
         * 企业规模。
         */
        private String enterpriseScale;
        /**
         * 所属行业。
         */
        private String industry;
        /**
         * 成立年份。
         */
        private Integer establishYear;
        /**
         * 登记证书有效截止日期。
         */
        private String certificateValidUntil;
        /**
         * 登记地址。
         */
        private String registerAddress;
        /**
         * 办公/经营地址。
         */
        private String operatingAddress;
        /**
         * 存续状态。
         */
        private String businessStatus;
        /**
         * 注册资本折人民币合计。
         */
        private BigDecimal registeredCapital;
    }

    @Data
    public static class UnsettledLoan {
        /**
         * 账户编号。
         */
        private String accountNo;
        /**
         * 授信机构。
         */
        private String institution;
        /**
         * 业务种类。
         */
        private String businessType;
        /**
         * 开立日期。
         */
        private String openDate;
        /**
         * 到期日。
         */
        private String dueDate;
        /**
         * 币种。
         */
        private String currency;
        /**
         * 借款金额。
         */
        private BigDecimal loanAmount;
        /**
         * 发放形式。
         */
        private String disbursementForm;
        /**
         * 担保方式。
         */
        private String guaranteeType;
        /**
         * 余额。
         */
        private BigDecimal balance;
        /**
         * 五级分类。
         */
        private String fiveClass;
        /**
         * 逾期总额。
         */
        private BigDecimal overdueTotal;
        /**
         * 逾期本金。
         */
        private BigDecimal overduePrincipal;
        /**
         * 逾期月数。
         */
        private Integer overdueMonths;
        /**
         * 最近一次还款日期。
         */
        private String lastRepaymentDate;
        /**
         * 最近一次还款总额。
         */
        private BigDecimal lastRepaymentAmount;
        /**
         * 最近一次还款形式。
         */
        private String lastRepaymentForm;
        /**
         * 信息报告日期。
         */
        private String reportDate;
    }
}
