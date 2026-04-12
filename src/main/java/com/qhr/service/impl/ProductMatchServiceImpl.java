package com.qhr.service.impl;

import com.qhr.model.Product;
import com.qhr.model.ProductRule;
import com.qhr.service.CreditProductService;
import com.qhr.service.ProductMatchService;
import com.qhr.service.ProductRuleTemplateService;
import com.qhr.vo.ApplicantProfile;
import com.qhr.vo.match.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@ApplicationScoped
public class ProductMatchServiceImpl implements ProductMatchService {

    private final ProductRuleTemplateService productRuleTemplateService;
    private final CreditProductService creditProductService;

    public ProductMatchServiceImpl(ProductRuleTemplateService productRuleTemplateService,
                                   CreditProductService creditProductService) {
        this.productRuleTemplateService = productRuleTemplateService;
        this.creditProductService = creditProductService;
    }

    @Override
    public ProductMatchSummary match(ApplicantProfile applicantProfile, ApplicationContext applicationContext) {
        ProductMatchSummary summary = new ProductMatchSummary();
        for (ProductRuleTemplateDraft draft : productRuleTemplateService.listBuiltInTemplates()) {
            ProductMatchResult result = evaluateDraft(draft, applicantProfile, applicationContext);
            summary.getResults().add(result);
            if (result.getStatus() == ProductMatchStatus.MATCH && result.getProductId() != null) {
                summary.getMatchedProductIds().add(result.getProductId());
            }
            if ((result.getStatus() == ProductMatchStatus.MATCH || result.getStatus() == ProductMatchStatus.REVIEW)
                    && result.getProductId() != null) {
                summary.getCandidateProductIds().add(result.getProductId());
            }
        }
        return summary;
    }

    private ProductMatchResult evaluateDraft(ProductRuleTemplateDraft draft,
                                             ApplicantProfile profile,
                                             ApplicationContext context) {
        ProductMatchResult result = new ProductMatchResult();
        result.setBankName(draft.getBankName());
        result.setProductName(draft.getProductName());
        ProductRule rule = draft.getRule();
        if (rule != null) {
            result.setRuleId(rule.getId());
            result.setRuleVersion(rule.getRuleVersion());
        }

        Product product = creditProductService.findByBankNameAndProductName(draft.getBankName(), draft.getProductName());
        if (product != null) {
            result.setProductId(product.getId());
        }

        List<ProductMatchReason> rejectReasons = new ArrayList<>();
        List<ProductMatchReason> insufficientReasons = new ArrayList<>();
        List<ProductMatchReason> reviewReasons = new ArrayList<>();

        evaluateBasic(draft, profile, rejectReasons, insufficientReasons);
        evaluateCredit(draft, profile, rejectReasons, insufficientReasons);
        evaluateContext(draft, profile, context, reviewReasons);

        if (!rejectReasons.isEmpty()) {
            result.setStatus(ProductMatchStatus.REJECT);
            result.getReasons().addAll(rejectReasons);
            return result;
        }
        if (!insufficientReasons.isEmpty()) {
            result.setStatus(ProductMatchStatus.INSUFFICIENT_DATA);
            result.getReasons().addAll(insufficientReasons);
            insufficientReasons.stream()
                    .map(ProductMatchReason::getSourceField)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(result.getMissingFields()::add);
            return result;
        }
        if (!reviewReasons.isEmpty()) {
            result.setStatus(ProductMatchStatus.REVIEW);
            result.getReasons().addAll(reviewReasons);
            reviewReasons.stream()
                    .map(ProductMatchReason::getMessage)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(result.getRequiredActions()::add);
            return result;
        }
        result.setStatus(ProductMatchStatus.MATCH);
        return result;
    }

    private void evaluateBasic(ProductRuleTemplateDraft draft,
                               ApplicantProfile profile,
                               List<ProductMatchReason> rejectReasons,
                               List<ProductMatchReason> insufficientReasons) {
        ProductRule rule = draft.getRule();
        ProductRuleExtra extra = draft.getExtra();

        if (extra != null && extra.getAllowedRegions() != null && !extra.getAllowedRegions().isEmpty()) {
            if (isBlank(profile.getCompanyRegion())) {
                insufficientReasons.add(insufficient("REGION_MISSING", "缺少企业地区信息", "BasicGate",
                        "companyRegion", extra.getAllowedRegions(), null));
            } else if (!matchRegion(profile.getCompanyRegion(), extra.getAllowedRegions())) {
                rejectReasons.add(reject("REGION_NOT_MATCH", "企业地区不在产品准入范围内", "BasicGate",
                        "companyRegion", extra.getAllowedRegions(), profile.getCompanyRegion()));
            }
        }

        if (extra != null && extra.getMinAge() != null) {
            if (profile.getAge() == null) {
                insufficientReasons.add(insufficient("AGE_MISSING", "缺少申请人年龄信息", "BasicGate",
                        "age", extra.getMinAge(), null));
            } else if (profile.getAge() < extra.getMinAge()) {
                rejectReasons.add(reject("AGE_TOO_LOW", "申请人年龄低于产品下限", "BasicGate",
                        "age", extra.getMinAge(), profile.getAge()));
            }
        }
        if (extra != null && extra.getMaxAge() != null) {
            if (profile.getAge() == null) {
                insufficientReasons.add(insufficient("AGE_MISSING", "缺少申请人年龄信息", "BasicGate",
                        "age", extra.getMaxAge(), null));
            } else if (profile.getAge() > extra.getMaxAge()) {
                rejectReasons.add(reject("AGE_TOO_HIGH", "申请人年龄高于产品上限", "BasicGate",
                        "age", extra.getMaxAge(), profile.getAge()));
            }
        }

        if (rule != null && rule.getGsMinEstablishMonths() != null) {
            if (profile.getCompanyAgeMonths() == null) {
                insufficientReasons.add(insufficient("COMPANY_AGE_MISSING", "缺少企业成立时长信息", "BasicGate",
                        "companyAgeMonths", rule.getGsMinEstablishMonths(), null));
            } else if (profile.getCompanyAgeMonths() < rule.getGsMinEstablishMonths()) {
                rejectReasons.add(reject("COMPANY_TOO_YOUNG", "企业成立时间未达产品要求", "BasicGate",
                        "companyAgeMonths", rule.getGsMinEstablishMonths(), profile.getCompanyAgeMonths()));
            }
        }

        if (extra != null && extra.getRequiredIndustryKeywords() != null && !extra.getRequiredIndustryKeywords().isEmpty()) {
            if (isBlank(profile.getIndustry()) && (profile.getIndustryTags() == null || profile.getIndustryTags().isEmpty())) {
                insufficientReasons.add(insufficient("INDUSTRY_MISSING", "缺少企业行业信息", "BasicGate",
                        "industry", extra.getIndustryRestrictionDescription(), null));
            } else if (!matchIndustry(profile, extra.getRequiredIndustryKeywords())) {
                rejectReasons.add(reject("INDUSTRY_NOT_MATCH", "企业行业不符合产品限制", "BasicGate",
                        "industry", extra.getIndustryRestrictionDescription(), profile.getIndustry()));
            }
        }

        if (rule != null && rule.getGsMinLegalRepChangeGapMonths() != null && profile.getLegalPersonLastChangeGapMonths() != null
                && profile.getLegalPersonLastChangeGapMonths() < rule.getGsMinLegalRepChangeGapMonths()) {
            rejectReasons.add(reject("LEGAL_REP_CHANGE_TOO_RECENT", "法人最近一次变更时间过近", "SubjectGate",
                    "legalPersonLastChangeGapMonths", rule.getGsMinLegalRepChangeGapMonths(),
                    profile.getLegalPersonLastChangeGapMonths()));
        }
    }

    private void evaluateCredit(ProductRuleTemplateDraft draft,
                                ApplicantProfile profile,
                                List<ProductMatchReason> rejectReasons,
                                List<ProductMatchReason> insufficientReasons) {
        ProductRule rule = draft.getRule();
        ProductRuleExtra extra = draft.getExtra();

        if (rule != null && rule.getPcMaxNonBankLoanCount() != null) {
            if (profile.getPersonalOnlineSmallLoanCount() == null) {
                insufficientReasons.add(insufficient("NON_BANK_LOAN_MISSING", "缺少非银行网贷/小贷笔数", "CreditGate",
                        "personalOnlineSmallLoanCount", rule.getPcMaxNonBankLoanCount(), null));
            } else if (profile.getPersonalOnlineSmallLoanCount() > rule.getPcMaxNonBankLoanCount()) {
                rejectReasons.add(reject("NON_BANK_LOAN_TOO_MANY", "非银行网贷/小贷笔数超限", "CreditGate",
                        "personalOnlineSmallLoanCount", rule.getPcMaxNonBankLoanCount(),
                        profile.getPersonalOnlineSmallLoanCount()));
            }
        }

        if (extra != null && Boolean.FALSE.equals(extra.getAllowAbnormalCreditAccount())
                && Boolean.TRUE.equals(profile.getAbnormalCreditAccount())) {
            rejectReasons.add(reject("ABNORMAL_ACCOUNT_NOT_ALLOWED", "征信账户状态异常，不符合产品要求", "CreditGate",
                    "abnormalCreditAccount", false, true));
        }
    }

    private void evaluateContext(ProductRuleTemplateDraft draft,
                                 ApplicantProfile profile,
                                 ApplicationContext context,
                                 List<ProductMatchReason> reviewReasons) {
        ProductRuleExtra extra = draft.getExtra();
        if (extra == null) {
            return;
        }
        if (!isBlank(extra.getApplyChannel()) && !equalsIgnoreCase(extra.getApplyChannel(), context == null ? null : context.getApplyChannel())) {
            reviewReasons.add(review("APPLY_CHANNEL_CONFIRM", "需按产品要求走" + extra.getApplyChannel() + "申请", "ContextGate",
                    "applyChannel", extra.getApplyChannel(), context == null ? null : context.getApplyChannel()));
        }
        if (!isBlank(extra.getDisbursementAccountType())
                && !equalsIgnoreCase(extra.getDisbursementAccountType(), context == null ? null : context.getDisbursementAccountType())) {
            reviewReasons.add(review("DISBURSEMENT_ACCOUNT_CONFIRM", "需确认放款账户满足" + extra.getDisbursementAccountType(), "ContextGate",
                    "disbursementAccountType", extra.getDisbursementAccountType(),
                    context == null ? null : context.getDisbursementAccountType()));
        }
        if (Boolean.TRUE.equals(extra.getRequireOnsiteInvestigation())
                && !Boolean.TRUE.equals(context == null ? null : context.getAcceptOnsiteInvestigation())) {
            reviewReasons.add(review("ONSITE_CONFIRM", "需确认可接受下户实地调查", "ContextGate",
                    "acceptOnsiteInvestigation", true, context == null ? null : context.getAcceptOnsiteInvestigation()));
        }
        if (Boolean.TRUE.equals(extra.getRequireSpouseJointLiability())
                && !Boolean.TRUE.equals(context == null ? null : context.getSpouseJointLiabilityAvailable())) {
            reviewReasons.add(review("SPOUSE_JOINT_LIABILITY_CONFIRM", "需确认配偶可连带", "ContextGate",
                    "spouseJointLiabilityAvailable", true,
                    context == null ? null : context.getSpouseJointLiabilityAvailable()));
        }
        if (Boolean.TRUE.equals(extra.getAcceptInternalAccounts())
                && !Boolean.TRUE.equals(profile.getCanProvideInternalAccounts())) {
            reviewReasons.add(review("INTERNAL_ACCOUNTS_CONFIRM", "需确认可补充内账资料", "TaxGate",
                    "canProvideInternalAccounts", true, profile.getCanProvideInternalAccounts()));
        }
    }

    private boolean matchRegion(String actualRegion, List<String> allowedRegions) {
        String normalizedActual = normalizeRegion(actualRegion);
        for (String allowedRegion : allowedRegions) {
            if (normalizedActual.contains(normalizeRegion(allowedRegion))) {
                return true;
            }
        }
        return false;
    }

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

    private ProductMatchReason reject(String code, String message, String node, String field, Object expected, Object actual) {
        ProductMatchReason reason = baseReason(code, message, node, field, expected, actual);
        reason.setHardReject(true);
        return reason;
    }

    private ProductMatchReason insufficient(String code, String message, String node, String field, Object expected, Object actual) {
        ProductMatchReason reason = baseReason(code, message, node, field, expected, actual);
        reason.setHardReject(false);
        return reason;
    }

    private ProductMatchReason review(String code, String message, String node, String field, Object expected, Object actual) {
        ProductMatchReason reason = baseReason(code, message, node, field, expected, actual);
        reason.setHardReject(false);
        return reason;
    }

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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.equalsIgnoreCase(right);
    }
}
