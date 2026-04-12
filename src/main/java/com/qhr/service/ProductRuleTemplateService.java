package com.qhr.service;

import com.qhr.vo.match.ProductRuleTemplateDraft;

import java.util.List;

public interface ProductRuleTemplateService {

    List<ProductRuleTemplateDraft> listBuiltInTemplates();

    ProductRuleTemplateDraft findBuiltInTemplate(String bankName, String productName);
}
