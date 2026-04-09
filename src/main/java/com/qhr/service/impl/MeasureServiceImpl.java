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
import lombok.NonNull;

import java.time.LocalDate;

@ApplicationScoped
public class MeasureServiceImpl implements MeasureService {

    private final EnterpriseService enterpriseService;
    private final UserService userService;
    private final FinancingIntentionService financingIntentionService;
    private final MeasureAsyncMatchService measureAsyncMatchService;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public MeasureServiceImpl(EnterpriseService enterpriseService, UserService userService, FinancingIntentionService financingIntentionService, MeasureAsyncMatchService measureAsyncMatchService, TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.enterpriseService = enterpriseService;
        this.userService = userService;
        this.financingIntentionService = financingIntentionService;
        this.measureAsyncMatchService = measureAsyncMatchService;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    private static @NonNull FinancingIntention getFinancingIntention(MeasureSubmitRequest request,
                                                                     String openid) {
        FinancingIntention intention = new FinancingIntention();
        intention.setUserOpenId(openid);
        intention.setEnterpriseId(request.enterpriseId());
        intention.setAmountRange(request.amountRange());
        intention.setPersonalCreditName(request.personalCreditName());
        intention.setPersonalCreditCloudId(request.personalCreditCloudId());
        intention.setEnterpriseCreditName(request.enterpriseCreditName());
        intention.setEnterpriseCreditCloudId(request.enterpriseCreditCloudId());
        return intention;
    }

    @Override
    public PrecheckResult precheck(EnterprisePayload request, String openid, String unionid) {
        //预审判断 是否满一年
        if (!LocalDate.parse(request.startDate()).isAfter(LocalDate.now().minusYears(1))) {
            return new PrecheckResult(null, request.name(), false, "企业注册日期需满1年");
        }
        //是否正常经营
        if (!request.status().contains("在业") || request.status().contains("存续")) {
            return new PrecheckResult(null, request.name(), false, "企业状态需为在业或存续");
        }

        //用户-保存
        userService.create(openid, unionid);
        //企业-更新/保存
        Long enterpriseId = upsertEnterprise(request);
        //中间表-用户绑定企业
        userService.bindEnterprise(openid, enterpriseId);
        //预审通过
        return new PrecheckResult(enterpriseId, request.name(), true, "预审通过");
    }

    @Override
    public String submit(MeasureSubmitRequest request, String openid, String unionid) {
        //融资需求-保存
        FinancingIntention intention = getFinancingIntention(request, openid);
        Long intentionId = financingIntentionService.create(intention);

        //异步执行-查询财税数据+申请人画像封装+产品匹配
        triggerAfterCommit(request, openid, request.enterpriseId(), intentionId);
        //实时返回结果，不影响异步执行
        return "SUCCESS";
    }

    private Long upsertEnterprise(EnterprisePayload enterprisePayload) {
        //根据creditCode查询是否存在
        Enterprise existing = enterpriseService.getByCreditCode(enterprisePayload.creditCode());

        if (existing == null) {
            Enterprise enterprise = new Enterprise();
            enterprise.setName(enterprisePayload.name());
            enterprise.setCreditCode(enterprisePayload.creditCode());
            enterprise.setStartDate(enterprisePayload.startDate());
            enterprise.setOperName(enterprisePayload.operName());
            enterprise.setStatus(enterprisePayload.status());
            enterprise.setAddress(enterprisePayload.address());
            return enterpriseService.create(enterprise);
        }

        existing.setName(enterprisePayload.name());
        existing.setCreditCode(enterprisePayload.creditCode());
        existing.setStartDate(enterprisePayload.startDate());
        existing.setOperName(enterprisePayload.operName());
        existing.setStatus(enterprisePayload.status());
        existing.setAddress(enterprisePayload.address());
        ApiAssert.isTrue(enterpriseService.update(existing), ApiCode.INTERNAL_ERROR, "更新企业信息失败");
        return existing.getId();
    }

    private void triggerAfterCommit(MeasureSubmitRequest request,
                                    String openid,
                                    Long enterpriseId,
                                    Long intentionId) {
        //当前事务的上下文工具箱
        transactionSynchronizationRegistry.registerInterposedSynchronization(new Synchronization() {
            //事务成功前
            @Override
            public void beforeCompletion() {
            }

            //事务成功后
            @Override
            public void afterCompletion(int status) {
                if (status == Status.STATUS_COMMITTED) {
                    measureAsyncMatchService.trigger(request, openid, enterpriseId, intentionId);
                }
            }
        });
    }
}
