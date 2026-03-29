package com.qhr.service;

import com.qhr.config.PageResult;
import com.qhr.model.ProductRule;

import java.util.List;

public interface ProductRuleService {

  List<ProductRule> list();

  PageResult<ProductRule> list(Integer offset, Integer size);

  ProductRule getById(Long id);

  Long create(ProductRule productRule);

  boolean update(ProductRule productRule);

  boolean delete(Long id);
}
