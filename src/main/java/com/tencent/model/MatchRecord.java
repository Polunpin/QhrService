package com.tencent.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配记录实体。
 */
public record MatchRecord(Long id,
                          String userId,
                          Long enterpriseId,
                          Long intentionId,
                          String productIds,
                          BigDecimal matchScore,
                          String riskType,
                          String riskLevel,
                          String status,
                          LocalDateTime createdAt) implements Serializable, WithId<MatchRecord> {

    /**
     * 复制并替换id。
     */
    public MatchRecord withId(Long id) {
        return new MatchRecord(id, userId, enterpriseId, intentionId, productIds,
                matchScore, riskType, riskLevel, status, createdAt);
    }
}
