package com.qhr.service.impl;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.model.Enterprise;
import com.qhr.model.FinancingIntention;
import com.qhr.service.*;
import com.qhr.vo.Person;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

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
    public Object submit(MeasureSubmitRequest request, String openid, String unionid) {
        //保存用户
        userService.create(openid, unionid);
        //更新或保存企业
        Long enterpriseId = upsertEnterprise(request.enterprise());
        ApiAssert.isTrue(userService.bindEnterprise(openid, enterpriseId, "owner"),
                ApiCode.INTERNAL_ERROR, "用户与企业绑定失败");

        FinancingIntention intention = new FinancingIntention();
        intention.setId(enterpriseId);
        intention.setAmountRange(request.amountRange());
        intention.setProperty(request.property());
        intention.setPropertyMortgage(request.propertyMortgage());
        intention.setSpouseSupport(request.spouseSupport());
        intention.setTaxAccount(request.taxAccount());
        intention.setTaxPassword(request.taxPassword());
        financingIntentionService.create(intention);

        Object precheck = dmnDecisionService.precheck(buildPrecheckPerson(request.enterprise()));
        ApiAssert.notNull(precheck, ApiCode.INTERNAL_ERROR, "预审结果为空");
        return precheck;
    }

    private Long upsertEnterprise(MeasureSubmitRequest.EnterprisePayload enterprisePayload) {
        //查询企业是否存在
        Enterprise existing = findExistingEnterprise(enterprisePayload.creditCode());

        if (existing == null) {
            Enterprise enterprise = new Enterprise();
            enterprise.setName(enterprisePayload.name());
            enterprise.setCreditCode(enterprisePayload.creditCode());
            enterprise.setStartDate(enterprisePayload.startDate());
            enterprise.setOperName(enterprisePayload.operName());
            enterprise.setStatus(enterprisePayload.status());
            enterprise.setAddress(enterprisePayload.address());
            return enterpriseService.create(enterprise);
        } else {
            existing.setName(enterprisePayload.name());
            existing.setCreditCode(enterprisePayload.creditCode());
            existing.setStartDate(enterprisePayload.startDate());
            existing.setOperName(enterprisePayload.operName());
            existing.setStatus(enterprisePayload.status());
            existing.setAddress(enterprisePayload.address());
            ApiAssert.isTrue(enterpriseService.update(existing), ApiCode.INTERNAL_ERROR, "更新企业信息失败");
        }

        return existing.getId();
    }

    private Enterprise findExistingEnterprise(String creditCode) {
        //根据creditCode查询是否存在
        return enterpriseService.getByCreditCode(creditCode);
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
