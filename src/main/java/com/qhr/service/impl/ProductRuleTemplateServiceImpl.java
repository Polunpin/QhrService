package com.qhr.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.model.ProductRule;
import com.qhr.service.ProductRuleTemplateService;
import com.qhr.vo.match.ProductRuleExtra;
import com.qhr.vo.match.ProductRuleTemplateDraft;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ProductRuleTemplateServiceImpl implements ProductRuleTemplateService {

    private static final String SOURCE = "企业贷模型版产品字典260326.xlsx";

    private final ObjectMapper objectMapper;

    public ProductRuleTemplateServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ProductRuleTemplateDraft> listBuiltInTemplates() {
        return List.of(buildBocAgriTradeDraft(), buildBocProvidentFundDraft());
    }

    @Override
    public ProductRuleTemplateDraft findBuiltInTemplate(String bankName, String productName) {
        return listBuiltInTemplates().stream()
                .filter(item -> equalsIgnoreCase(item.getBankName(), bankName))
                .filter(item -> equalsIgnoreCase(item.getProductName(), productName))
                .findFirst()
                .orElse(null);
    }

    private ProductRuleTemplateDraft buildBocAgriTradeDraft() {
        ProductRule rule = baseRule("中国银行-农贸贷-v1");
        rule.setGsMinEstablishMonths(24);
        rule.setGsMinLegalRepChangeGapMonths(0);
        rule.setPcMaxOverdueMonths24m(3);

        ProductRuleExtra extra = new ProductRuleExtra();
        extra.setAllowedRegions(List.of("北京"));
        extra.setMinAge(18);
        extra.setMaxAge(80);
        extra.setDisbursementAccountType("对公户");
        extra.setRequireOnsiteInvestigation(true);
        extra.setRequiredIndustryKeywords(List.of("农贸", "批发", "肉", "蛋", "奶"));
        extra.setIndustryRestrictionDescription("只限农贸批发，肉蛋奶相关");
        extra.setRequireSpouseJointLiability(true);
        extra.setMaxLastFullYearDebtRatio(BigDecimal.ONE);
        extra.setAcceptInternalAccounts(true);
        extra.setMaxOverdueCount24m(6);
        extra.setAllowAbnormalCreditAccount(true);

        return buildDraft("中国银行", "农贸贷", rule, extra);
    }

    private ProductRuleTemplateDraft buildBocProvidentFundDraft() {
        ProductRule rule = baseRule("中国银行-公积金-v1");
        rule.setGsMinEstablishMonths(24);
        rule.setGsMinLegalRepChangeGapMonths(0);
        rule.setPcMaxOverdueMonths24m(3);
        rule.setPcMaxNonBankLoanCount(99);

        ProductRuleExtra extra = new ProductRuleExtra();
        extra.setAllowedRegions(List.of("北京"));
        extra.setMinAge(18);
        extra.setMaxAge(80);
        extra.setApplyChannel("线下");
        extra.setDisbursementAccountType("对公户");
        extra.setRequireOnsiteInvestigation(true);
        extra.setRequireSpouseJointLiability(true);
        extra.setMaxLastFullYearDebtRatio(new BigDecimal("0.85"));
        extra.setMaxOverdueCount24m(6);
        extra.setAllowAbnormalCreditAccount(true);

        return buildDraft("中国银行", "公积金", rule, extra);
    }

    private ProductRule baseRule(String ruleName) {
        ProductRule rule = new ProductRule();
        rule.setRuleVersion(1);
        rule.setRuleName(ruleName);
        rule.setIsActive(1);
        rule.setIndRequireRegionMatch(1);
        return rule;
    }

    private ProductRuleTemplateDraft buildDraft(String bankName,
                                                String productName,
                                                ProductRule rule,
                                                ProductRuleExtra extra) {
        ProductRuleTemplateDraft draft = new ProductRuleTemplateDraft();
        draft.setBankName(bankName);
        draft.setProductName(productName);
        draft.setSource(SOURCE);
        draft.setRule(rule);
        draft.setExtra(extra);
        String extJson = writeExtra(extra);
        draft.setExtJson(extJson);
        rule.setExtJson(extJson);
        return draft;
    }

    private String writeExtra(ProductRuleExtra extra) {
        try {
            return objectMapper.writeValueAsString(extra);
        } catch (JsonProcessingException exception) {
            throw new ApiException(ApiCode.INTERNAL_ERROR, "序列化ProductRuleExtra失败");
        }
    }

    private boolean equalsIgnoreCase(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.toLowerCase(Locale.ROOT).equals(right.toLowerCase(Locale.ROOT));
    }
}
