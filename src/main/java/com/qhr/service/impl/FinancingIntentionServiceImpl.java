package com.qhr.service.impl;

import com.qhr.dao.FinancingIntentionsMapper;
import com.qhr.model.FinancingIntention;
import com.qhr.service.FinancingIntentionService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FinancingIntentionServiceImpl implements FinancingIntentionService {

  private final FinancingIntentionsMapper financingIntentionsMapper;

  public FinancingIntentionServiceImpl(FinancingIntentionsMapper financingIntentionsMapper) {
    this.financingIntentionsMapper = financingIntentionsMapper;
  }

  @Override
  public List<FinancingIntention> list(Long enterpriseId, Integer offset, Integer size) {
    return financingIntentionsMapper.list(enterpriseId, offset, size);
  }

  @Override
  public Long create(FinancingIntention intention) {
    financingIntentionsMapper.insert(intention);
    return intention.getId();
  }

  @Override
  public boolean update(FinancingIntention intention) {
    return financingIntentionsMapper.updateById(intention) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return financingIntentionsMapper.deleteById(id) > 0;
  }

  @Override
  public FinancingIntention getById(Long id) {
    return financingIntentionsMapper.selectById(id);
  }


}
