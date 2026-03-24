package com.qhr.service.impl;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.dto.MeasureSubmitResponse;
import com.qhr.model.Enterprise;
import com.qhr.model.FinancingIntention;
import com.qhr.service.DmnDecisionService;
import com.qhr.service.EnterpriseService;
import com.qhr.service.FinancingIntentionService;
import com.qhr.service.MeasureService;
import com.qhr.service.UserService;
import com.qhr.vo.Person;
import com.qhr.vo.PrecheckResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ApplicationScoped
public class MeasureServiceImpl implements MeasureService {

  private final UserService userService;
  private final EnterpriseService enterpriseService;
  private final FinancingIntentionService financingIntentionService;
  private final DmnDecisionService dmnDecisionService;

  public MeasureServiceImpl(UserService userService,
                            EnterpriseService enterpriseService,
                            FinancingIntentionService financingIntentionService,
                            DmnDecisionService dmnDecisionService) {
    this.userService = userService;
    this.enterpriseService = enterpriseService;
    this.financingIntentionService = financingIntentionService;
    this.dmnDecisionService = dmnDecisionService;
  }

  @Override
  @Transactional
  public MeasureSubmitResponse submit(MeasureSubmitRequest request, String openid, String unionid) {
    ApiAssert.notNull(request, ApiCode.BAD_REQUEST, "请求体不能为空");
    ApiAssert.notNull(request.enterprise(), ApiCode.BAD_REQUEST, "企业信息不能为空");

    //保存用户
    Long userId = userService.create(openid, unionid);
    //保存企业
    Long enterpriseId = upsertEnterprise(request.enterprise());
    //todo 逻辑可简化
    ApiAssert.isTrue(userService.bindEnterprise(openid, enterpriseId, "owner"),
        ApiCode.INTERNAL_ERROR, "用户与企业绑定失败");

    FinancingIntention intention = new FinancingIntention(
        null,
        enterpriseId,
        request.amountRange(),
        defaultFalse(request.property()),
        defaultFalse(request.propertyMortgage()),
        defaultFalse(request.spouseSupport()),
        request.taxAccount(),
        request.taxPassword(),
        null,
        null
    );
    financingIntentionService.create(intention);

    //todo 预审
//    PrecheckResult precheckResult = (PrecheckResult) dmnDecisionService.precheck(buildPrecheckPerson(request.enterprise()));
//    ApiAssert.notNull(precheckResult, ApiCode.INTERNAL_ERROR, "预审结果为空");
    return new MeasureSubmitResponse(true, "测试");
  }

  private Long upsertEnterprise(MeasureSubmitRequest.EnterprisePayload enterprisePayload) {
//    ApiAssert.isTrue(enterprisePayload.name() != null && !enterprisePayload.name().isBlank(),
//        ApiCode.BAD_REQUEST, "企业名称不能为空");

    Enterprise existing = findExistingEnterprise(enterprisePayload.creditCode());
    Enterprise enterprise = new Enterprise(
        existing == null ? null : existing.id(),
        enterprisePayload.name(),
        blankToNull(enterprisePayload.creditCode()),
        blankToNull(enterprisePayload.startDate()),
        blankToNull(enterprisePayload.operName()),
        blankToNull(enterprisePayload.status()),
        blankToNull(enterprisePayload.address()),
        existing == null ? null : existing.createdAt(),
        existing == null ? null : existing.updatedAt()
    );

    if (existing == null) {
      return enterpriseService.create(enterprise);
    }
//    ApiAssert.isTrue(enterpriseService.update(enterprise), ApiCode.INTERNAL_ERROR, "更新企业信息失败");
    return existing.id();
  }

  private Enterprise findExistingEnterprise(String creditCode) {
    if (creditCode == null || creditCode.isBlank()) {
      return null;
    }
    List<Enterprise> items = enterpriseService.list(null, creditCode, null, null, 0, 1);
    return items.isEmpty() ? null : items.getFirst();
  }

  private Person buildPrecheckPerson(MeasureSubmitRequest.EnterprisePayload enterprisePayload) {
    return new Person(
        normalizeAddressForPrecheck(enterprisePayload.address()),
        blankToNull(enterprisePayload.status()),
        calculateEnterpriseAgeMonths(enterprisePayload.startDate()),
        "",
        false,
        false,
        false
    );
  }

  private BigDecimal calculateEnterpriseAgeMonths(String startDate) {
    if (startDate == null || startDate.isBlank()) {
      return BigDecimal.ZERO;
    }
    try {
      LocalDate establishedOn = LocalDate.parse(startDate);
      LocalDate current = LocalDate.now();
      long months = ChronoUnit.MONTHS.between(
          establishedOn.withDayOfMonth(1),
          current.withDayOfMonth(1)
      );
      return BigDecimal.valueOf(Math.max(0, months));
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException("企业成立日期格式不正确，应为yyyy-MM-dd");
    }
  }

  private String normalizeAddressForPrecheck(String address) {
    if (address == null || address.isBlank()) {
      return "";
    }
    return address.contains("北京") ? "北京" : address;
  }

  private boolean defaultFalse(Boolean value) {
    return Boolean.TRUE.equals(value);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
