package com.qhr.vo.match;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 候选粗筛规则 payload。
 * 只承接最适合在应用层先做缩圈的基础规则。
 */
@Data
public class CandidateFilterPayload {

    /**
     * 允许准入的地区列表，如北京、上海
     */
    private List<String> allowedRegions = new ArrayList<>();
    /**
     * 要求命中的行业关键词列表，命中任一关键词即可认为行业匹配
     */
    private List<String> requiredIndustryKeywords = new ArrayList<>();
    /**
     * 行业限制的人类可读描述，用于前端和人工解释
     */
    private String industryRestrictionDescription;

    /**
     * 最低年龄
     */
    private Integer minAge;
    /**
     * 最高年龄
     */
    private Integer maxAge;
    /**
     * 最低成立月数
     */
    private Integer minEstablishMonths;
    /**
     * 最低注册资本
     */
    private BigDecimal minRegisteredCapital;
    /**
     * 最低实缴资本
     */
    private BigDecimal minPaidInCapital;

    /**
     * 近2年法人变更次数上限
     */
    private Integer maxLegalRepChangeCount2y;
    /**
     * 法人最近一次变更距今的最少月数
     */
    private Integer minLegalRepChangeGapMonths;
    /**
     * 法人最低持股比例
     */
    private BigDecimal minLegalRepShareRatio;
    /**
     * 是否允许法人不连带
     */
    private Boolean allowLegalRepNoJointLiability;
    /**
     * 是否允许由股东替代法人连带
     */
    private Boolean allowShareholderReplaceJointLiability;
    /**
     * 股东替代连带时的最低持股比例
     */
    private BigDecimal minShareholderReplaceShareRatio;
    /**
     * 是否要求配偶可连带
     */
    private Boolean requireSpouseJointLiability;

    /**
     * 申请方式要求，如线下、线上
     */
    private String applyChannel;
    /**
     * 放款账户要求，如对公户
     */
    private String disbursementAccountType;
    /**
     * 是否要求接受下户实地调查
     */
    private Boolean requireOnsiteInvestigation;
    /**
     * 是否要求可补合同/发票/收据等申请资料
     */
    private Boolean requireContractInvoiceReceipt;
    /**
     * 最低公积金连续缴存月数
     */
    private Integer minProvidentFundMonths;
}
