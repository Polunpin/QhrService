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

