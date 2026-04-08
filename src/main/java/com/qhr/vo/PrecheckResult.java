package com.qhr.vo;

public record PrecheckResult(
        /*企业ID*/
        Long enterpriseId,
        /*企业名称*/
        String enterpriseName,
        /*结果*/
        Boolean result,
        //原因
        String why){
}