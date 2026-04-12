package com.qhr.service.impl;

import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.model.ProductRule;
import com.qhr.service.ProductMatchService;
import com.qhr.service.ProductRuleService;
import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.match.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@ApplicationScoped
public class ProductMatchServiceImpl implements ProductMatchService {

    private static final System.Logger LOGGER = System.getLogger(ProductMatchServiceImpl.class.getName());

    private final ProductRuleService productRuleService;

    public ProductMatchServiceImpl(ProductRuleService productRuleService) {
        this.productRuleService = productRuleService;
    }

    /**
     * 执行整批产品规则匹配。
     * 规则来源固定为数据库中的 ACTIVE 规则，数据库是唯一规则源。
     */
    @Override
    public ProductMatchSummary match(ApplicantProfile applicantProfile, ApplicationContext applicationContext) {
        ProductMatchSummary summary = new ProductMatchSummary();
        for (ProductRule rule : loadRulesForMatching()) {
            ProductMatchResult result = evaluateRule(rule, applicantProfile, applicationContext);
            if (result.getStatus() == ProductMatchStatus.MATCH && result.getProductId() != null) {
                summary.getProductIds().add(result.getProductId());
            }
            if (result.getStatus() == ProductMatchStatus.REVIEW) {
                summary.getReviewReasons().addAll(result.getReasons());
            }
            if (result.getStatus() == ProductMatchStatus.REJECT
                    || result.getStatus() == ProductMatchStatus.INSUFFICIENT_DATA) {
                summary.getRejectReasons().addAll(result.getReasons());
            }
        }
        return summary;
    }

    /**
     * 读取当前时点可用的 ACTIVE 规则。
     * 若数据库中没有可用规则，直接中断匹配，避免系统在无规则状态下继续运行。
     */
    private List<ProductRule> loadRulesForMatching() {
        List<ProductRule> activeRules = productRuleService.listActive();
        if (activeRules.isEmpty()) {
            LOGGER.log(System.Logger.Level.ERROR, "yw_product_rule 中没有 ACTIVE 规则，无法执行产品匹配");
            throw new ApiException(ApiCode.INTERNAL_ERROR, "没有可用的产品规则");
        }
        return activeRules;
    }

    /**
     * 针对单条产品规则执行完整匹配，汇总拒绝、缺失和 REVIEW 原因，得出最终状态。
     */
    private ProductMatchResult evaluateRule(ProductRule rule,
                                            ApplicantProfile profile,
                                            ApplicationContext context) {
        ProductMatchResult result = new ProductMatchResult();
        result.setProductId(rule.getProductId());

        List<ProductMatchReason> rejectReasons = new ArrayList<>();
        List<ProductMatchReason> insufficientReasons = new ArrayList<>();
        List<ProductMatchReason> reviewReasons = new ArrayList<>();

        CandidateFilterPayload candidateFilter = rule.getCandidateFilter();
        MatchRulePayload matchRule = rule.getMatchRule();

        evaluateCandidateFilter(candidateFilter, profile, rejectReasons, insufficientReasons);
        evaluateMatchRule(matchRule, profile, rejectReasons, insufficientReasons, reviewReasons);
        evaluateContext(candidateFilter, matchRule, profile, context, reviewReasons);

        if (!rejectReasons.isEmpty()) {
            result.setStatus(ProductMatchStatus.REJECT);
            result.getReasons().addAll(tagReasons(rejectReasons, result.getProductId(), ProductMatchStatus.REJECT));
            return result;
        }
        if (!insufficientReasons.isEmpty()) {
            result.setStatus(ProductMatchStatus.INSUFFICIENT_DATA);
            result.getReasons().addAll(tagReasons(insufficientReasons, result.getProductId(), ProductMatchStatus.INSUFFICIENT_DATA));
            return result;
        }
        if (!reviewReasons.isEmpty()) {
            result.setStatus(ProductMatchStatus.REVIEW);
            result.getReasons().addAll(tagReasons(reviewReasons, result.getProductId(), ProductMatchStatus.REVIEW));
            return result;
        }
        result.setStatus(ProductMatchStatus.MATCH);
        return result;
    }

    /**
     * 执行候选粗筛规则。
     * 这一层只处理最适合先缩圈的基础条件，例如地区、年龄、成立时长、行业、公积金等。
     */
    private void evaluateCandidateFilter(CandidateFilterPayload filter,
                                         ApplicantProfile profile,
                                         List<ProductMatchReason> rejectReasons,
                                         List<ProductMatchReason> insufficientReasons) {
        if (filter == null) {
            return;
        }

        if (!filter.getAllowedRegions().isEmpty()) {
            if (isBlank(profile.getCompanyRegion())) {
                insufficientReasons.add(insufficient("REGION_MISSING", "缺少企业地区信息", "BasicGate",
                        "companyRegion", filter.getAllowedRegions(), null));
            } else if (!matchRegion(profile.getCompanyRegion(), filter.getAllowedRegions())) {
                rejectReasons.add(reject("REGION_NOT_MATCH", "企业地区不在产品准入范围内", "BasicGate",
                        "companyRegion", filter.getAllowedRegions(), profile.getCompanyRegion()));
            }
        }

        compareMin(filter.getMinAge(), profile.getAge(), "AGE_MISSING", "AGE_TOO_LOW",
                "缺少申请人年龄信息", "申请人年龄低于产品下限", "BasicGate", "age",
                rejectReasons, insufficientReasons);
        compareMax(filter.getMaxAge(), profile.getAge(), "AGE_MISSING", "AGE_TOO_HIGH",
                "缺少申请人年龄信息", "申请人年龄高于产品上限", "BasicGate", "age",
                rejectReasons, insufficientReasons);
        compareMin(filter.getMinEstablishMonths(), profile.getCompanyAgeMonths(), "COMPANY_AGE_MISSING", "COMPANY_TOO_YOUNG",
                "缺少企业成立时长信息", "企业成立时间未达产品要求", "BasicGate", "companyAgeMonths",
                rejectReasons, insufficientReasons);

        compareMin(filter.getMinRegisteredCapital(), profile.getRegisteredCapital(), "REGISTERED_CAPITAL_MISSING", "REGISTERED_CAPITAL_TOO_LOW",
                "缺少注册资本信息", "注册资本低于产品要求", "BasicGate", "registeredCapital",
                rejectReasons, insufficientReasons);
        compareMin(filter.getMinPaidInCapital(), profile.getPaidInCapital(), "PAID_IN_CAPITAL_MISSING", "PAID_IN_CAPITAL_TOO_LOW",
                "缺少实缴资本信息", "实缴资本低于产品要求", "BasicGate", "paidInCapital",
                rejectReasons, insufficientReasons);
        compareMin(filter.getMinLegalRepShareRatio(), profile.getLegalPersonShareRatio(), "LEGAL_REP_SHARE_MISSING", "LEGAL_REP_SHARE_TOO_LOW",
                "缺少法人持股信息", "法人持股比例低于产品要求", "SubjectGate", "legalPersonShareRatio",
                rejectReasons, insufficientReasons);
        compareMin(filter.getMinLegalRepChangeGapMonths(), profile.getLegalPersonLastChangeGapMonths(),
                "LEGAL_REP_CHANGE_GAP_MISSING", "LEGAL_REP_CHANGE_TOO_RECENT",
                "缺少法人最近变更时间信息", "法人最近一次变更时间过近", "SubjectGate", "legalPersonLastChangeGapMonths",
                rejectReasons, insufficientReasons);
        compareMax(filter.getMaxLegalRepChangeCount2y(), profile.getLegalPersonChangeCount2y(),
                "LEGAL_REP_CHANGE_COUNT_MISSING", "LEGAL_REP_CHANGE_TOO_MANY",
                "缺少近2年法人变更次数信息", "近2年法人变更次数超限", "SubjectGate", "legalPersonChangeCount2y",
                rejectReasons, insufficientReasons);

        if (!filter.getRequiredIndustryKeywords().isEmpty()) {
            if (isBlank(profile.getIndustry()) && (profile.getIndustryTags() == null || profile.getIndustryTags().isEmpty())) {
                insufficientReasons.add(insufficient("INDUSTRY_MISSING", "缺少企业行业信息", "BasicGate",
                        "industry", filter.getIndustryRestrictionDescription(), null));
            } else if (!matchIndustry(profile, filter.getRequiredIndustryKeywords())) {
                rejectReasons.add(reject("INDUSTRY_NOT_MATCH", "企业行业不符合产品限制", "BasicGate",
                        "industry", filter.getIndustryRestrictionDescription(), profile.getIndustry()));
            }
        }

        compareMin(filter.getMinProvidentFundMonths(), profile.getProvidentFundContributionMonths(),
                "PROVIDENT_FUND_MISSING", "PROVIDENT_FUND_NOT_ENOUGH",
                "缺少公积金连续缴存月数", "公积金连续缴存月数未达产品要求", "BasicGate", "providentFundContributionMonths",
                rejectReasons, insufficientReasons);
    }

    /**
     * 执行详细匹配规则。
     * 这一层处理税务、个人征信等精判逻辑，并将可补充材料类条件落到 REVIEW。
     */
    private void evaluateMatchRule(MatchRulePayload payload,
                                   ApplicantProfile profile,
                                   List<ProductMatchReason> rejectReasons,
                                   List<ProductMatchReason> insufficientReasons,
                                   List<ProductMatchReason> reviewReasons) {
        if (payload == null) {
            return;
        }

        MatchRulePayload.PersonalCreditRule personal = payload.getPersonalCredit();
        compareMax(personal.getMaxQueryCount1m(), profile.getReviewQueryCount1m(),
                "PC_QUERY_1M_MISSING", "PC_QUERY_1M_TOO_MANY",
                "缺少近1个月个人查询次数", "近1个月个人查询次数超限", "CreditGate", "reviewQueryCount1m",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxQueryCount3m(), profile.getReviewQueryCount3m(),
                "PC_QUERY_3M_MISSING", "PC_QUERY_3M_TOO_MANY",
                "缺少近3个月个人查询次数", "近3个月个人查询次数超限", "CreditGate", "reviewQueryCount3m",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxQueryCount6m(), profile.getReviewQueryCount6m(),
                "PC_QUERY_6M_MISSING", "PC_QUERY_6M_TOO_MANY",
                "缺少近6个月个人查询次数", "近6个月个人查询次数超限", "CreditGate", "reviewQueryCount6m",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxQueryCount12m(), profile.getReviewQueryCount12m(),
                "PC_QUERY_12M_MISSING", "PC_QUERY_12M_TOO_MANY",
                "缺少近12个月个人查询次数", "近12个月个人查询次数超限", "CreditGate", "reviewQueryCount12m",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxLoanQueryCount1m(), profile.getLoanQueryCount1m(),
                "PC_LOAN_QUERY_1M_MISSING", "PC_LOAN_QUERY_1M_TOO_MANY",
                "缺少近1个月贷款审批查询次数", "近1个月贷款审批查询次数超限", "CreditGate", "loanQueryCount1m",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxCreditCardQueryCount1m(), profile.getCreditCardQueryCount1m(),
                "PC_CARD_QUERY_1M_MISSING", "PC_CARD_QUERY_1M_TOO_MANY",
                "缺少近1个月信用卡审批查询次数", "近1个月信用卡审批查询次数超限", "CreditGate", "creditCardQueryCount1m",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxNonBankLoanCount(), profile.getPersonalOnlineSmallLoanCount(),
                "NON_BANK_LOAN_MISSING", "NON_BANK_LOAN_TOO_MANY",
                "缺少非银行网贷/小贷笔数", "非银行网贷/小贷笔数超限", "CreditGate", "personalOnlineSmallLoanCount",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxTotalOverdueTerms5y(), profile.getOverdueTerms5y(),
                "OVERDUE_TERMS_5Y_MISSING", "OVERDUE_TERMS_5Y_TOO_MANY",
                "缺少近5年累计逾期期数", "近5年累计逾期期数超限", "CreditGate", "overdueTerms5y",
                rejectReasons, insufficientReasons);
        compareMax(personal.getMaxCreditCardUtilization(), profile.getCreditCardUtilizationCurrent(),
                "CARD_UTILIZATION_MISSING", "CARD_UTILIZATION_TOO_HIGH",
                "缺少当前信用卡使用率", "当前信用卡使用率超限", "CreditGate", "creditCardUtilizationCurrent",
                rejectReasons, insufficientReasons);

        if (Boolean.FALSE.equals(personal.getAllowAbnormalCreditAccount())
                && Boolean.TRUE.equals(profile.getAbnormalCreditAccount())) {
            rejectReasons.add(reject("ABNORMAL_ACCOUNT_NOT_ALLOWED", "个人征信账户状态异常，不符合产品要求", "CreditGate",
                    "abnormalCreditAccount", false, true));
        }

        MatchRulePayload.TaxRule tax = payload.getTax();
        compareMin(tax.getMinInvoiceAmount12m(), profile.getInvoiceAmount12m(),
                "INVOICE_12M_MISSING", "INVOICE_12M_TOO_LOW",
                "缺少近12个月开票金额", "近12个月开票金额低于产品要求", "TaxGate", "invoiceAmount12m",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinInvoiceAmountYtd(), profile.getInvoiceAmountCurrentYear(),
                "INVOICE_YTD_MISSING", "INVOICE_YTD_TOO_LOW",
                "缺少当年开票金额", "当年开票金额低于产品要求", "TaxGate", "invoiceAmountCurrentYear",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinInvoiceAmountLastYear(), profile.getInvoiceAmountLastYear(),
                "INVOICE_LAST_YEAR_MISSING", "INVOICE_LAST_YEAR_TOO_LOW",
                "缺少上一自然年开票金额", "上一自然年开票金额低于产品要求", "TaxGate", "invoiceAmountLastYear",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinInvoiceMonths12m(), profile.getInvoiceMonths12m(),
                "INVOICE_MONTHS_MISSING", "INVOICE_MONTHS_NOT_ENOUGH",
                "缺少近12个月开票月数", "近12个月开票月数未达要求", "TaxGate", "invoiceMonths12m",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinTaxMonths12m(), profile.getTaxMonths12m(),
                "TAX_MONTHS_12M_MISSING", "TAX_MONTHS_12M_NOT_ENOUGH",
                "缺少近12个月纳税月数", "近12个月纳税月数未达要求", "TaxGate", "taxMonths12m",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinTaxMonths24m(), profile.getTaxMonths24m(),
                "TAX_MONTHS_24M_MISSING", "TAX_MONTHS_24M_NOT_ENOUGH",
                "缺少近24个月纳税月数", "近24个月纳税月数未达要求", "TaxGate", "taxMonths24m",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinTaxAmount12m(), profile.getTaxAmount12m(),
                "TAX_AMOUNT_12M_MISSING", "TAX_AMOUNT_12M_TOO_LOW",
                "缺少近12个月纳税金额", "近12个月纳税金额低于产品要求", "TaxGate", "taxAmount12m",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinTaxAmountYtd(), profile.getTaxAmountCurrentYear(),
                "TAX_AMOUNT_YTD_MISSING", "TAX_AMOUNT_YTD_TOO_LOW",
                "缺少当年纳税金额", "当年纳税金额低于产品要求", "TaxGate", "taxAmountCurrentYear",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinTaxAmountLastYear(), profile.getTaxAmountLastYear(),
                "TAX_AMOUNT_LAST_YEAR_MISSING", "TAX_AMOUNT_LAST_YEAR_TOO_LOW",
                "缺少上一自然年纳税金额", "上一自然年纳税金额低于产品要求", "TaxGate", "taxAmountLastYear",
                rejectReasons, insufficientReasons);
        compareMax(tax.getMaxTaxBurdenRatio(), profile.getTaxBurdenRate1y(),
                "TAX_BURDEN_RATIO_MISSING", "TAX_BURDEN_RATIO_TOO_HIGH",
                "缺少税负率", "税负率高于产品要求", "TaxGate", "taxBurdenRate1y",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinTaxBurdenRatioYoy(), profile.getTaxBurdenRateRatio25vs24(),
                "TAX_BURDEN_RATIO_YOY_MISSING", "TAX_BURDEN_RATIO_YOY_TOO_LOW",
                "缺少税负率同比信息", "税负率同比低于产品要求", "TaxGate", "taxBurdenRateRatio25vs24",
                rejectReasons, insufficientReasons);
        compareMin(tax.getMinTotalDeclares(), profile.getTotalTaxDeclarationCount(),
                "TOTAL_DECLARES_MISSING", "TOTAL_DECLARES_NOT_ENOUGH",
                "缺少累计纳税申报次数", "累计纳税申报次数未达要求", "TaxGate", "totalTaxDeclarationCount",
                rejectReasons, insufficientReasons);

        if (Boolean.FALSE.equals(tax.getAllowInternalAccounts())
                && Boolean.TRUE.equals(profile.getCanProvideInternalAccounts())) {
            reviewReasons.add(review("INTERNAL_ACCOUNTS_NOT_ACCEPTED", "当前产品不接受内账资料，需确认可用外部报表", "TaxGate",
                    "canProvideInternalAccounts", false, true));
        }
    }

    /**
     * 评估申请上下文相关条件。
     * 这类条件通常不是画像事实，而是申请动作是否可执行，因此优先给 REVIEW 而非直接拒绝。
     */
    private void evaluateContext(CandidateFilterPayload filter,
                                 MatchRulePayload payload,
                                 ApplicantProfile profile,
                                 ApplicationContext context,
                                 List<ProductMatchReason> reviewReasons) {
        if (filter != null) {
            if (!isBlank(filter.getApplyChannel())
                    && !equalsIgnoreCase(filter.getApplyChannel(), context == null ? null : context.getApplyChannel())) {
                reviewReasons.add(review("APPLY_CHANNEL_CONFIRM", "需按产品要求走" + filter.getApplyChannel() + "申请", "ContextGate",
                        "applyChannel", filter.getApplyChannel(), context == null ? null : context.getApplyChannel()));
            }
            if (!isBlank(filter.getDisbursementAccountType())
                    && !equalsIgnoreCase(filter.getDisbursementAccountType(), context == null ? null : context.getDisbursementAccountType())) {
                reviewReasons.add(review("DISBURSEMENT_ACCOUNT_CONFIRM", "需确认放款账户满足" + filter.getDisbursementAccountType(), "ContextGate",
                        "disbursementAccountType", filter.getDisbursementAccountType(),
                        context == null ? null : context.getDisbursementAccountType()));
            }
            if (Boolean.TRUE.equals(filter.getRequireOnsiteInvestigation())
                    && !Boolean.TRUE.equals(context == null ? null : context.getAcceptOnsiteInvestigation())) {
                reviewReasons.add(review("ONSITE_CONFIRM", "需确认可接受下户实地调查", "ContextGate",
                        "acceptOnsiteInvestigation", true, context == null ? null : context.getAcceptOnsiteInvestigation()));
            }
            if (Boolean.TRUE.equals(filter.getRequireSpouseJointLiability())
                    && !Boolean.TRUE.equals(context == null ? null : context.getSpouseJointLiabilityAvailable())) {
                reviewReasons.add(review("SPOUSE_JOINT_LIABILITY_CONFIRM", "需确认配偶可连带", "ContextGate",
                        "spouseJointLiabilityAvailable", true,
                        context == null ? null : context.getSpouseJointLiabilityAvailable()));
            }
        }

        if (payload != null && payload.getReviewRules() != null) {
            for (MatchRulePayload.ReviewRule reviewRule : payload.getReviewRules()) {
                if (reviewRule == null || isBlank(reviewRule.getMessage())) {
                    continue;
                }
                reviewReasons.add(review(reviewRule.getCode(), reviewRule.getMessage(), "ReviewGate",
                        reviewRule.getField(), reviewRule.getExpectedValue(), null));
            }
        }

        if (Boolean.TRUE.equals(payload == null ? null : payload.getTax().getAllowInternalAccounts())
                && !Boolean.TRUE.equals(profile.getCanProvideInternalAccounts())) {
            reviewReasons.add(review("INTERNAL_ACCOUNTS_CONFIRM", "需确认可补充内账资料", "TaxGate",
                    "canProvideInternalAccounts", true, profile.getCanProvideInternalAccounts()));
        }
    }

    /**
     * 判断企业地区是否命中允许区域列表。
     * 当前策略为“标准化后包含匹配”，适合省/市混输场景。
     */
    private boolean matchRegion(String actualRegion, List<String> allowedRegions) {
        String normalizedActual = normalizeRegion(actualRegion);
        for (String allowedRegion : allowedRegions) {
            if (normalizedActual.contains(normalizeRegion(allowedRegion))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对地区文本做轻量标准化，去掉常见行政区后缀，降低输入差异对匹配的影响。
     */
    private String normalizeRegion(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("省", "")
                .replace("市", "")
                .replace("自治区", "")
                .replace("壮族", "")
                .replace("回族", "")
                .replace("维吾尔", "")
                .replace("特别行政区", "")
                .trim();
    }

    /**
     * 判断企业行业信息是否命中任一行业关键词。
     * 行业主类和行业标签都会参与匹配。
     */
    private boolean matchIndustry(ApplicantProfile profile, List<String> keywords) {
        StringBuilder text = new StringBuilder();
        if (!isBlank(profile.getIndustry())) {
            text.append(profile.getIndustry()).append(' ');
        }
        if (profile.getIndustryTags() != null) {
            profile.getIndustryTags().stream().filter(Objects::nonNull).forEach(tag -> text.append(tag).append(' '));
        }
        String normalized = text.toString().toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通用最小值比较。
     * 规则值存在但画像值缺失时记为 INSUFFICIENT_DATA，画像值低于阈值时记为 REJECT。
     */
    private <T extends Number> void compareMin(T expected, T actual,
                                               String missingCode, String rejectCode,
                                               String missingMessage, String rejectMessage,
                                               String node, String field,
                                               List<ProductMatchReason> rejectReasons,
                                               List<ProductMatchReason> insufficientReasons) {
        if (expected == null) {
            return;
        }
        if (actual == null) {
            insufficientReasons.add(insufficient(missingCode, missingMessage, node, field, expected, null));
            return;
        }
        if (toBigDecimal(actual).compareTo(toBigDecimal(expected)) < 0) {
            rejectReasons.add(reject(rejectCode, rejectMessage, node, field, expected, actual));
        }
    }

    /**
     * 通用最大值比较。
     * 规则值存在但画像值缺失时记为 INSUFFICIENT_DATA，画像值高于阈值时记为 REJECT。
     */
    private <T extends Number> void compareMax(T expected, T actual,
                                               String missingCode, String rejectCode,
                                               String missingMessage, String rejectMessage,
                                               String node, String field,
                                               List<ProductMatchReason> rejectReasons,
                                               List<ProductMatchReason> insufficientReasons) {
        if (expected == null) {
            return;
        }
        if (actual == null) {
            insufficientReasons.add(insufficient(missingCode, missingMessage, node, field, expected, null));
            return;
        }
        if (toBigDecimal(actual).compareTo(toBigDecimal(expected)) > 0) {
            rejectReasons.add(reject(rejectCode, rejectMessage, node, field, expected, actual));
        }
    }

    /**
     * 将任意 Number 统一转成 BigDecimal，便于复用比较逻辑。
     */
    private BigDecimal toBigDecimal(Number value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 构造硬拒绝原因。
     */
    private ProductMatchReason reject(String code, String message, String node, String field, Object expected, Object actual) {
        ProductMatchReason reason = baseReason(code, message, node, field, expected, actual);
        reason.setHardReject(true);
        return reason;
    }

    /**
     * 构造缺失数据原因。
     */
    private ProductMatchReason insufficient(String code, String message, String node, String field, Object expected, Object actual) {
        ProductMatchReason reason = baseReason(code, message, node, field, expected, actual);
        reason.setHardReject(false);
        return reason;
    }

    /**
     * 构造 REVIEW 原因。
     */
    private ProductMatchReason review(String code, String message, String node, String field, Object expected, Object actual) {
        ProductMatchReason reason = baseReason(code, message, node, field, expected, actual);
        reason.setHardReject(false);
        return reason;
    }

    /**
     * 构造通用匹配原因对象。
     */
    private ProductMatchReason baseReason(String code, String message, String node, String field, Object expected, Object actual) {
        ProductMatchReason reason = new ProductMatchReason();
        reason.setCode(code);
        reason.setMessage(message);
        reason.setDecisionNode(node);
        reason.setSourceField(field);
        reason.setExpected(expected);
        reason.setActual(actual);
        return reason;
    }

    /**
     * 为原因列表补齐产品上下文，便于在汇总后仍能定位原因属于哪个产品。
     */
    private List<ProductMatchReason> tagReasons(List<ProductMatchReason> reasons,
                                                Long productId,
                                                ProductMatchStatus reasonType) {
        for (ProductMatchReason reason : reasons) {
            reason.setProductId(productId);
            reason.setReasonType(reasonType);
        }
        return reasons;
    }

    /**
     * 判断字符串是否为空白。
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * 忽略大小写比较两个字符串。
     */
    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }
}
