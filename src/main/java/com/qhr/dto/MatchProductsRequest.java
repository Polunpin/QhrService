package com.qhr.dto;

import java.math.BigDecimal;

/**
 * 产品匹配请求。
 */
public record MatchProductsRequest(BigDecimal expectedAmount,
                                   Integer expectedTerm,
                                   String regionCode,
                                   String productType) {
}
