package com.qhr.service.impl;

import com.qhr.dto.EnterprisePayload;
import com.qhr.dto.MeasureSubmitRequest;
import com.qhr.service.MeasureService;
import com.qhr.vo.PrecheckResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@ApplicationScoped
public class MeasureServiceImpl implements MeasureService {

    private final MeasureSubmitTxService measureSubmitTxService;

    public MeasureServiceImpl(MeasureSubmitTxService measureSubmitTxService) {
        this.measureSubmitTxService = measureSubmitTxService;
    }

    @Override
    public PrecheckResult submit(MeasureSubmitRequest request, String openid, String unionid) {
        //预审判断 是否满一年，是否正常经营
        PrecheckResult precheck = evaluatePrecheck(request.enterprise());
        //失败立即返回，不触发事务
        if (!Boolean.TRUE.equals(precheck.result())) {
            return precheck;
        }
        //进入业务，触发事务
        return measureSubmitTxService.submitAfterPrecheck(request, openid, unionid);
    }

    private PrecheckResult evaluatePrecheck(EnterprisePayload enterprise) {
        if (!isEstablishedMoreThanOneYear(enterprise.startDate())) {
            return new PrecheckResult(null, enterprise.name(), false, "企业注册日期需满1年");
        }

        if (!isActiveEnterpriseStatus(enterprise.status())) {
            return new PrecheckResult(null, enterprise.name(), false, "企业状态需为在业或存续");
        }

        return new PrecheckResult(null, enterprise.name(), true, "预审通过");
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
}
