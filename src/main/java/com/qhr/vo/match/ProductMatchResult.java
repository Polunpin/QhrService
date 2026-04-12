package com.qhr.vo.match;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单产品匹配结果。
 * 当前主要用于匹配器内部汇总过程，不再作为前端主返回对象。
 */
@Data
public class ProductMatchResult {

    /**
     * 对应产品ID；若产品主数据缺失则可能为空
     */
    private Long productId;
    /** 匹配结果状态：MATCH / REVIEW / REJECT / INSUFFICIENT_DATA */
    private ProductMatchStatus status;
    /** 当前产品对应的结构化原因列表 */
    private List<ProductMatchReason> reasons = new ArrayList<>();
}
