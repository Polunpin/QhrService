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
  public FinancingIntention getById(Long id) {
    return financingIntentionsMapper.getById(id);
  }

  @Override
  public Long create(FinancingIntention intention) {
    financingIntentionsMapper.insert(intention);
    return financingIntentionsMapper.lastInsertId();
  }

  @Override
  public boolean update(FinancingIntention intention) {
    return financingIntentionsMapper.update(intention) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return financingIntentionsMapper.delete(id) > 0;
  }

  @Override
  public List<FinancingIntention> list(Long enterpriseId, Integer offset, Integer size) {
    return financingIntentionsMapper.list(enterpriseId, offset, size);
  }

  @Override
  public long count(Long enterpriseId) {
    return financingIntentionsMapper.count(enterpriseId);
  }
}
