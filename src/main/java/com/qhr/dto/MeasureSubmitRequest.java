package com.qhr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MeasureSubmitRequest(
        //融资区间
        String amountRange,
        //个人征信文件名称
        String personalCreditName,
        //个人征信文件云托管文件ID
        String personalCreditCloudId,
        //企业征信文件名称
        String enterpriseCreditName,
        //企业征信文件云托管文件ID
        String enterpriseCreditCloudId,
        //qcc订单号
        String orderNo,
        //企业基本信息
        EnterprisePayload enterprise
) {
}
