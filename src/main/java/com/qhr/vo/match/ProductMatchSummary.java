package com.qhr.vo.match;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量产品匹配汇总。
 */
@Data
public class ProductMatchSummary {

    private List<ProductMatchResult> results = new ArrayList<>();
    private List<Long> matchedProductIds = new ArrayList<>();
    private List<Long> candidateProductIds = new ArrayList<>();
}
