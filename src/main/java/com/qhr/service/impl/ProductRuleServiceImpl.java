package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.ProductRulesMapper;
import com.qhr.model.ProductRule;
import com.qhr.service.ProductRuleService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ProductRuleServiceImpl implements ProductRuleService {

  private final ProductRulesMapper productRulesMapper;

  public ProductRuleServiceImpl(ProductRulesMapper productRulesMapper) {
    this.productRulesMapper = productRulesMapper;
  }


  @Override
  public PageResult<ProductRule> list(Integer offset, Integer size) {
    List<ProductRule> items = productRulesMapper.list(offset, size);
    long total = productRulesMapper.selectCount(Wrappers.lambdaQuery());
    return PageResult.of(items, total, offset, size);
  }

  @Override
  public Long create(ProductRule productRule) {
    productRulesMapper.insert(productRule);
    return productRule.getId();
  }

  @Override
  public boolean update(ProductRule productRule) {
    ProductRule current = productRulesMapper.selectById(productRule.getId());
    if (current == null) {
      return false;
    }
    return productRulesMapper.updateById(productRule) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return productRulesMapper.deleteById(id) > 0;
  }

  @Override
  public List<ProductRule> list() {
    return productRulesMapper.selectList(Wrappers.lambdaQuery());
  }

  @Override
  public ProductRule getById(Long id) {
    return productRulesMapper.selectById(id);
  }

}
