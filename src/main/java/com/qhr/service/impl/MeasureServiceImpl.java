package com.qhr.service.impl;

import com.qhr.config.ApiAssert;
import com.qhr.config.ApiCode;
import com.qhr.dto.EnterprisePayload;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.model.Enterprise;
import com.qhr.model.FinancingIntention;
import com.qhr.service.EnterpriseService;
import com.qhr.service.FinancingIntentionService;
import com.qhr.service.MeasureService;
import com.qhr.service.UserService;
import com.qhr.vo.PrecheckResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@ApplicationScoped
public class MeasureServiceImpl implements MeasureService {

    private final UserService userService;
    private final EnterpriseService enterpriseService;
    private final FinancingIntentionService financingIntentionService;
    private final MeasureAsyncMatchService measureAsyncMatchService;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public MeasureServiceImpl(UserService userService,
                              EnterpriseService enterpriseService,
                              FinancingIntentionService financingIntentionService,
                              MeasureAsyncMatchService measureAsyncMatchService,
                              TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.userService = userService;
        this.enterpriseService = enterpriseService;
        this.financingIntentionService = financingIntentionService;
        this.measureAsyncMatchService = measureAsyncMatchService;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    @Override
    @Transactional
    public Object submit(MeasureSubmitRequest request, String openid, String unionid) {
        //保存用户
        userService.create(openid, unionid);
        //更新或保存企业
        Long enterpriseId = upsertEnterprise(request.enterprise());
        //用户-绑定-企业
        ApiAssert.isTrue(userService.bindEnterprise(openid, enterpriseId, "1"),
                ApiCode.INTERNAL_ERROR, "用户与企业绑定失败");
        //融资需求参数封装
        FinancingIntention intention = new FinancingIntention();
        intention.setEnterpriseId(enterpriseId);
        intention.setAmountRange(request.amountRange());
        intention.setProperty(request.property());
        intention.setPropertyMortgage(request.propertyMortgage());
        intention.setSpouseSupport(request.spouseSupport());
        intention.setTaxAccount(request.taxAccount());
        intention.setTaxPassword(request.taxPassword());
        Long intentionId = financingIntentionService.create(intention);
        //预审
        PrecheckResult precheck = evaluatePrecheck(request.enterprise());
        if (precheck.result()) {
            triggerAfterCommit(request, openid, enterpriseId, intentionId);
        }
        return precheck;
    }

    private Long upsertEnterprise(EnterprisePayload enterprisePayload) {
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

    private PrecheckResult evaluatePrecheck(EnterprisePayload enterprise) {
        if (!isEstablishedMoreThanOneYear(enterprise.startDate())) {
            return new PrecheckResult(false, "企业注册日期需满1年");
        }

        if (!isActiveEnterpriseStatus(enterprise.status())) {
            return new PrecheckResult(false, "企业状态需为在业或存续");
        }

        return new PrecheckResult(true, "预审通过");
    }

    private boolean isEstablishedMoreThanOneYear(String startDate) {
        try {
            LocalDate establishedDate = LocalDate.parse(startDate);
            return !establishedDate.isAfter(LocalDate.now().minusYears(1));
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private boolean isActiveEnterpriseStatus(String status) {
        String normalized = status.trim();
        return normalized.contains("在业") || normalized.contains("存续");
    }

    private void triggerAfterCommit(MeasureSubmitRequest request,
                                    String openid,
                                    Long enterpriseId,
                                    Long intentionId) {
        transactionSynchronizationRegistry.registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
            }

            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    measureAsyncMatchService.trigger(request, openid, enterpriseId, intentionId);
                }
            }
        });
    }

}
