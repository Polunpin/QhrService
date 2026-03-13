package com.qhr.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 申请人测试。
 */
public record Person(
        /*公司名称*/
        @JsonProperty("status")
        String status,
        /*条件一：是否为注销*/
        @JsonProperty("t1")
        Boolean t1,
        /*条件二：是否为黑名单*/
        @JsonProperty("t2")
        Boolean t2) {
}