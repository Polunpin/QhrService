package com.qhr.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductRule implements Serializable {

  //主键
  Long id;

  //产品ID
  Long productId;

  //规则版本号
  Integer ruleVersion;

  //规则名称
  String ruleName;

  //是否生效
  Integer isActive;

  //生效开始时间
  LocalDateTime effectiveStartTime;

  //生效结束时间
  LocalDateTime effectiveEndTime;

  //备注
  String remark;

  //工商-最低成立月数
  Integer gsMinEstablishMonths;

  //工商-最低注册资本
  BigDecimal gsMinRegisteredCapital;

  //工商-最低实缴资本
  BigDecimal gsMinPaidInCapital;

  //工商-近2年法定代表人变更次数上限
  Integer gsMaxLegalRepChangeCount2y;

  //工商-法人最近一次变更距今最少月数
  Integer gsMinLegalRepChangeGapMonths;

  //工商-法人最低持股比例，按百分比存
  BigDecimal gsMinLegalRepShareRatio;

  //工商-是否允许法人不连带，1允许 0不允许 NULL不限制
  Integer gsAllowLegalRepNoJointLiability;

  //工商-是否允许股东代替法人连带，1允许 0不允许 NULL不限制
  Integer gsAllowShareholderReplaceJointLiability;

  //工商-股东代替连带时最低持股比例，按百分比存
  BigDecimal gsMinShareholderReplaceShareRatio;

  //税务-近12个月最少纳税月份数
  Integer taxMinTaxMonths12m;

  //税务-近24个月最少纳税月份数
  Integer taxMinTaxMonths24m;

  //税务-连续0申报次数上限
  Integer taxMaxZeroDeclareStreak;

  //税务-近12个月最低纳税金额
  BigDecimal taxMinTaxAmount12m;

  //税务-当年最低纳税金额
  BigDecimal taxMinTaxAmountYtd;

  //税务-上一完整自然年最低纳税金额
  BigDecimal taxMinTaxAmountLastYear;

  //税务-税负率上限，按百分比存
  BigDecimal taxMaxTaxBurdenRatio;

  //税务-当年税负率/上年税负率比值下限
  BigDecimal taxMinTaxBurdenRatioYoy;

  //税务-累计纳税申报次数下限
  Integer taxMinTotalDeclares;

  //司法-是否因重大诉讼拒绝
  Integer judRejectIfMajorLawsuit;

  //司法-是否因被执行拒绝
  Integer judRejectIfExecuted;

  //司法-是否因失信被执行拒绝
  Integer judRejectIfDishonestPerson;

  //司法-是否因股权冻结拒绝
  Integer judRejectIfEquityFrozen;

  //司法-近24个月被执行次数上限
  Integer judMaxExecutionCount24m;

  //司法-近12个月法院公告次数上限
  Integer judMaxCourtAnnouncementCount12m;

  //司法-近12个月行政处罚次数上限
  Integer judMaxAdminPenaltyCount12m;

  //司法-是否因重大司法风险拒绝
  Integer judRejectIfMajorJudicialRisk;

  //司法-是否因限制高消费拒绝
  Integer judRejectIfRestrictionHighConsumption;

  //行业风险-行业匹配模式，ALLOW/BLOCK
  String indMatchModeCode;

  //行业风险-行业风险等级上限，1低-5高
  Integer indMaxRiskLevel;

  //行业风险-是否要求企业标签
  Integer indRequireEnterpriseTag;

  //行业风险-标签最少剩余有效月数
  Integer indMinTagValidMonths;

  //行业风险-是否要求地区匹配
  Integer indRequireRegionMatch;

  //行业风险-是否要求经营地可核验
  Integer indRequireActualBusinessAddress;

  //行业风险-是否拒绝高污染行业
  Integer indRejectIfHighPollution;

  //行业风险-是否拒绝高耗能行业
  Integer indRejectIfHighEnergyConsumption;

  //行业风险-是否拒绝敏感行业
  Integer indRejectIfSensitiveIndustry;

  //企业征信-企业总负债上限
  BigDecimal ecMaxTotalLiability;

  //企业征信-企业信贷负债上限
  BigDecimal ecMaxCreditLiability;

  //企业征信-企业抵押负债上限
  BigDecimal ecMaxMortgageLiability;

  //企业征信-企业对外担保余额上限
  BigDecimal ecMaxExternalGuaranteeAmount;

  //企业征信-企业贷款机构数上限
  Integer ecMaxLoanOrgCount;

  //企业征信-近6个月企业征信查询次数上限
  Integer ecMaxQueryCount6m;

  //企业征信-近12个月逾期次数上限
  Integer ecMaxOverdueCount12m;

  //企业征信-近24个月最大逾期月数上限
  Integer ecMaxOverdueMonths24m;

  //企业征信-最短无逾期天数下限
  Integer ecMinNoOverdueDays;

  //企业征信-是否允许征信账户状态异常
  Integer ecAllowAbnormalCreditAccount;

  //个人征信-近1个月查询次数上限
  Integer pcMaxQueryCount1m;

  //个人征信-近3个月查询次数上限
  Integer pcMaxQueryCount3m;

  //个人征信-近6个月查询次数上限
  Integer pcMaxQueryCount6m;

  //个人征信-近1个月贷款审批查询次数上限
  Integer pcMaxLoanQueryCount1m;

  //个人征信-近1个月信用卡审批查询次数上限
  Integer pcMaxCreditCardQueryCount1m;

  //个人征信-近12个月逾期次数上限
  Integer pcMaxOverdueCount12m;

  //个人征信-近24个月最大逾期月数上限
  Integer pcMaxOverdueMonths24m;

  //个人征信-近5年累计逾期期数上限
  Integer pcMaxTotalOverdueTerms5y;

  //个人征信-非银行网贷/小贷笔数上限
  Integer pcMaxNonBankLoanCount;

  //个人征信-信用卡使用率上限，按百分比存
  BigDecimal pcMaxCreditCardUtilization;

  //低频个性化扩展字段，不建议通用DMN直接依赖
  String extJson;

  //创建时间
  LocalDateTime createdAt;

  //更新时间
  LocalDateTime updatedAt;
}
