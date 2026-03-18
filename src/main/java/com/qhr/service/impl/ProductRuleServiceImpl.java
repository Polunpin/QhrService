package com.qhr.service.impl;

import com.qhr.config.ApiCode;
import com.qhr.config.ApiException;
import com.qhr.dao.ProductRulesMapper;
import com.qhr.model.ProductRule;
import com.qhr.service.ProductRuleService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ProductRuleServiceImpl implements ProductRuleService {

  private final ProductRulesMapper productRulesMapper;

  public ProductRuleServiceImpl(ProductRulesMapper productRulesMapper) {
    this.productRulesMapper = productRulesMapper;
  }

  @Override
  public ProductRule getById(Long id) {
    return productRulesMapper.getById(id);
  }

  @Override
  public Long create(ProductRule productRule) {
    ProductRule normalized = normalizeForCreate(productRule);
    if (productRulesMapper.getByProductIdAndRuleVersion(normalized.productId(),
        normalized.ruleVersion()) != null) {
      throw new ApiException(ApiCode.CONFLICT, "产品规则版本已存在");
    }
    productRulesMapper.insert(normalized);
    return productRulesMapper.lastInsertId();
  }

  @Override
  public boolean update(ProductRule productRule) {
    ProductRule current = productRulesMapper.getById(requireId(productRule == null ? null : productRule.id()));
    if (current == null) {
      return false;
    }
    ProductRule normalized = normalizeForUpdate(productRule);
    Long targetProductId = normalized.productId() == null ? current.productId() : normalized.productId();
    Integer targetRuleVersion = normalized.ruleVersion() == null ? current.ruleVersion() : normalized.ruleVersion();
    ProductRule duplicated = productRulesMapper.getByProductIdAndRuleVersion(targetProductId, targetRuleVersion);
    if (duplicated != null && !duplicated.id().equals(normalized.id())) {
      throw new ApiException(ApiCode.CONFLICT, "产品规则版本已存在");
    }
    return productRulesMapper.update(normalized) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return productRulesMapper.delete(id) > 0;
  }

  @Override
  public List<ProductRule> list() {
    return productRulesMapper.list();
  }

  @Override
  public List<ProductRule> list(Long productId,
                                Integer ruleVersion,
                                String ruleName,
                                Integer isActive,
                                Integer offset,
                                Integer size) {
    return productRulesMapper.list(productId, ruleVersion, normalizeText(ruleName), isActive, offset, size);
  }

  @Override
  public long count(Long productId, Integer ruleVersion, String ruleName, Integer isActive) {
    return productRulesMapper.count(productId, ruleVersion, normalizeText(ruleName), isActive);
  }

  private ProductRule normalizeForCreate(ProductRule productRule) {
    if (productRule == null) {
      throw new ApiException(ApiCode.BAD_REQUEST, "请求体不能为空");
    }
    Long productId = requireId(productRule.productId(), "productId不能为空");
    Integer ruleVersion = productRule.ruleVersion() == null ? 1 : productRule.ruleVersion();
    String ruleName = defaultText(productRule.ruleName(), "default");
    Integer isActive = productRule.isActive() == null ? 1 : productRule.isActive();
    return normalizeRecord(new ProductRule(productRule.id(), productId, ruleVersion, ruleName, isActive,
        productRule.effectiveStartTime(), productRule.effectiveEndTime(), productRule.remark(),
        productRule.gsMinEstablishMonths(), productRule.gsMinRegisteredCapital(),
        productRule.gsMinPaidInCapital(), productRule.gsMaxLegalRepChangeCount2y(),
        productRule.gsMinLegalRepChangeGapMonths(), productRule.gsMinLegalRepShareRatio(),
        productRule.gsAllowLegalRepNoJointLiability(),
        productRule.gsAllowShareholderReplaceJointLiability(),
        productRule.gsMinShareholderReplaceShareRatio(), productRule.taxMinTaxMonths12m(),
        productRule.taxMinTaxMonths24m(), productRule.taxMaxZeroDeclareStreak(),
        productRule.taxMinTaxAmount12m(), productRule.taxMinTaxAmountYtd(),
        productRule.taxMinTaxAmountLastYear(), productRule.taxMaxTaxBurdenRatio(),
        productRule.taxMinTaxBurdenRatioYoy(), productRule.taxMinTotalDeclares(),
        productRule.judRejectIfMajorLawsuit(), productRule.judRejectIfExecuted(),
        productRule.judRejectIfDishonestPerson(), productRule.judRejectIfEquityFrozen(),
        productRule.judMaxExecutionCount24m(), productRule.judMaxCourtAnnouncementCount12m(),
        productRule.judMaxAdminPenaltyCount12m(), productRule.judRejectIfMajorJudicialRisk(),
        productRule.judRejectIfRestrictionHighConsumption(), productRule.indMatchModeCode(),
        productRule.indMaxRiskLevel(), productRule.indRequireEnterpriseTag(),
        productRule.indMinTagValidMonths(), productRule.indRequireRegionMatch(),
        productRule.indRequireActualBusinessAddress(), productRule.indRejectIfHighPollution(),
        productRule.indRejectIfHighEnergyConsumption(), productRule.indRejectIfSensitiveIndustry(),
        productRule.ecMaxTotalLiability(), productRule.ecMaxCreditLiability(),
        productRule.ecMaxMortgageLiability(), productRule.ecMaxExternalGuaranteeAmount(),
        productRule.ecMaxLoanOrgCount(), productRule.ecMaxQueryCount6m(),
        productRule.ecMaxOverdueCount12m(), productRule.ecMaxOverdueMonths24m(),
        productRule.ecMinNoOverdueDays(), productRule.ecAllowAbnormalCreditAccount(),
        productRule.pcMaxQueryCount1m(), productRule.pcMaxQueryCount3m(),
        productRule.pcMaxQueryCount6m(), productRule.pcMaxLoanQueryCount1m(),
        productRule.pcMaxCreditCardQueryCount1m(), productRule.pcMaxOverdueCount12m(),
        productRule.pcMaxOverdueMonths24m(), productRule.pcMaxTotalOverdueTerms5y(),
        productRule.pcMaxNonBankLoanCount(), productRule.pcMaxCreditCardUtilization(),
        productRule.extJson(), productRule.createdAt(), productRule.updatedAt()));
  }

  private ProductRule normalizeForUpdate(ProductRule productRule) {
    if (productRule == null) {
      throw new ApiException(ApiCode.BAD_REQUEST, "请求体不能为空");
    }
    requireId(productRule.id(), "id不能为空");
    return normalizeRecord(productRule);
  }

  private ProductRule normalizeRecord(ProductRule productRule) {
    return new ProductRule(productRule.id(), productRule.productId(), productRule.ruleVersion(),
        normalizeText(productRule.ruleName()), productRule.isActive(), productRule.effectiveStartTime(),
        productRule.effectiveEndTime(), normalizeText(productRule.remark()),
        productRule.gsMinEstablishMonths(), productRule.gsMinRegisteredCapital(),
        productRule.gsMinPaidInCapital(), productRule.gsMaxLegalRepChangeCount2y(),
        productRule.gsMinLegalRepChangeGapMonths(), productRule.gsMinLegalRepShareRatio(),
        productRule.gsAllowLegalRepNoJointLiability(),
        productRule.gsAllowShareholderReplaceJointLiability(),
        productRule.gsMinShareholderReplaceShareRatio(), productRule.taxMinTaxMonths12m(),
        productRule.taxMinTaxMonths24m(), productRule.taxMaxZeroDeclareStreak(),
        productRule.taxMinTaxAmount12m(), productRule.taxMinTaxAmountYtd(),
        productRule.taxMinTaxAmountLastYear(), productRule.taxMaxTaxBurdenRatio(),
        productRule.taxMinTaxBurdenRatioYoy(), productRule.taxMinTotalDeclares(),
        productRule.judRejectIfMajorLawsuit(), productRule.judRejectIfExecuted(),
        productRule.judRejectIfDishonestPerson(), productRule.judRejectIfEquityFrozen(),
        productRule.judMaxExecutionCount24m(), productRule.judMaxCourtAnnouncementCount12m(),
        productRule.judMaxAdminPenaltyCount12m(), productRule.judRejectIfMajorJudicialRisk(),
        productRule.judRejectIfRestrictionHighConsumption(), normalizeText(productRule.indMatchModeCode()),
        productRule.indMaxRiskLevel(), productRule.indRequireEnterpriseTag(),
        productRule.indMinTagValidMonths(), productRule.indRequireRegionMatch(),
        productRule.indRequireActualBusinessAddress(), productRule.indRejectIfHighPollution(),
        productRule.indRejectIfHighEnergyConsumption(), productRule.indRejectIfSensitiveIndustry(),
        productRule.ecMaxTotalLiability(), productRule.ecMaxCreditLiability(),
        productRule.ecMaxMortgageLiability(), productRule.ecMaxExternalGuaranteeAmount(),
        productRule.ecMaxLoanOrgCount(), productRule.ecMaxQueryCount6m(),
        productRule.ecMaxOverdueCount12m(), productRule.ecMaxOverdueMonths24m(),
        productRule.ecMinNoOverdueDays(), productRule.ecAllowAbnormalCreditAccount(),
        productRule.pcMaxQueryCount1m(), productRule.pcMaxQueryCount3m(),
        productRule.pcMaxQueryCount6m(), productRule.pcMaxLoanQueryCount1m(),
        productRule.pcMaxCreditCardQueryCount1m(), productRule.pcMaxOverdueCount12m(),
        productRule.pcMaxOverdueMonths24m(), productRule.pcMaxTotalOverdueTerms5y(),
        productRule.pcMaxNonBankLoanCount(), productRule.pcMaxCreditCardUtilization(),
        normalizeText(productRule.extJson()), productRule.createdAt(), productRule.updatedAt());
  }

  private Long requireId(Long value) {
    return requireId(value, "id不能为空");
  }

  private Long requireId(Long value, String message) {
    if (value == null) {
      throw new ApiException(ApiCode.BAD_REQUEST, message);
    }
    return value;
  }

  private String defaultText(String value, String defaultValue) {
    String normalized = normalizeText(value);
    return normalized == null ? defaultValue : normalized;
  }

  private static String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
