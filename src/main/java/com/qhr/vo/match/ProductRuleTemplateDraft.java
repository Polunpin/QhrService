package com.qhr.vo.match;

import com.qhr.model.ProductRule;
import lombok.Data;

/**
 * 产品规则模板草稿。
 * 用于把 Excel 中的规则先结构化成 ProductRule + ext_json，后续可直接入库。
 */
@Data
public class ProductRuleTemplateDraft {

    private String bankName;
    private String productName;
    private String source;
    private ProductRule rule;
    private ProductRuleExtra extra;
    private String extJson;
}
