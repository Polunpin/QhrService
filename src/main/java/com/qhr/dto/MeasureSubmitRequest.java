package com.qhr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MeasureSubmitRequest(
        //企业ID
        Long enterpriseId,

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

        //qcc-订单号
        String orderNo,
        //qcc-验证码
        String verifyCode,
        //qcc-数据下单状态，判断是否需要短信验证
        String dataStatus
) {
}

