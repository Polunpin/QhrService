package com.qhr.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配记录实体。
 */
@Data
public class MatchRecord implements Serializable {

    Long id;
    String userOpenId;
    Long enterpriseId;
    Long intentionId;
    String productIds;
    BigDecimal matchScore;
    String riskType;
    String riskLevel;
    String status;
    LocalDateTime createdAt;
}
