package com.qhr.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 非标服务订单实体。
 */
public record CustomServiceOrder(Long id,
                                 Long enterpriseId,
                                 Long intentionId,
                                 Long staffId,
                                 String currentStage,
                                 String serviceStatus,
                                 BigDecimal loanAmount,
                                 BigDecimal commissionAmount,
                                 BigDecimal serviceCost,
                                 String costDetails,
                                 String settleStatus,
                                 LocalDateTime lastUpdateAt,
                                 LocalDateTime createdAt) implements Serializable {

}
