package com.qhr.service;

import com.qhr.model.ProductRule;

import java.util.List;

public interface ProductRuleService {

  ProductRule getById(Long id);

  Long create(ProductRule productRule);

  boolean update(ProductRule productRule);

  boolean delete(Long id);

  List<ProductRule> list();

  List<ProductRule> list(Long productId,
                         Integer ruleVersion,
                         String ruleName,
                         Integer isActive,
                         Integer offset,
                         Integer size);

  long count(Long productId, Integer ruleVersion, String ruleName, Integer isActive);
}
