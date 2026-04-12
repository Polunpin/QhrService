package com.qhr.vo.match;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单产品匹配结果。
 */
@Data
public class ProductMatchResult {

    private Long productId;
    private String bankName;
    private String productName;
    private Long ruleId;
    private Integer ruleVersion;
    private ProductMatchStatus status;
    private List<ProductMatchReason> reasons = new ArrayList<>();
    private List<String> missingFields = new ArrayList<>();
    private List<String> requiredActions = new ArrayList<>();
}
