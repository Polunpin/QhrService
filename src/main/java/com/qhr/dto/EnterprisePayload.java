package com.qhr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * qcc模糊查询返回对象
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnterprisePayload(
        //地址
        @JsonProperty("Address")
        String address,
        //统一社会信用代码
        @JsonProperty("CreditCode")
        String creditCode,
        //主键
        @JsonProperty("KeyNo")
        String keyNo,
        //企业名称
        @JsonProperty("Name")
        String name,
        //注册号
        @JsonProperty("No")
        String no,
        //法定代表人姓名
        @JsonProperty("OperName")
        String operName,
        //成立日期
        @JsonProperty("StartDate")
        String startDate,
        //状态
        @JsonProperty("Status")
        String status
) {
}