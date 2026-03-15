package com.qhr.vo;

import java.math.BigDecimal;

/**
 * 申请人测试。
 */
public record Person(
        String address,
        String companyStatus,
        BigDecimal establishDate,
        String industry,
        Boolean abnormal,
        Boolean illegal,
        Boolean judicialRisk) {
}

//{
//        "address": "北京",
//        "companyStatus": "存续",
//        "establishDate": 18,
//        "industry": "科技服务",
//        "abnormal": false,
//        "illegal": false,
//        "judicialRisk": false
//        }
