package com.qhr.service.impl;

import com.qhr.dao.EnterprisesMapper;
import com.qhr.model.Enterprise;
import com.qhr.service.EnterpriseService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class EnterpriseServiceImpl implements EnterpriseService {

  private final EnterprisesMapper enterprisesMapper;

  public EnterpriseServiceImpl(EnterprisesMapper enterprisesMapper) {
    this.enterprisesMapper = enterprisesMapper;
  }

  @Override
  public Enterprise getById(Long id) {
    return enterprisesMapper.getById(id);
  }

  @Override
  public Long create(Enterprise enterprise) {
    enterprisesMapper.insert(enterprise);
    return enterprisesMapper.lastInsertId();
  }

  @Override
  public boolean update(Enterprise enterprise) {
    return enterprisesMapper.update(enterprise) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return enterprisesMapper.delete(id) > 0;
  }

  @Override
  public List<Enterprise> list(String name, String creditCode, String operName, String status, Integer offset, Integer size) {
    return enterprisesMapper.list(name, creditCode, operName, status, offset, size);
  }

  @Override
  public long count(String name, String creditCode, String operName, String status) {
    return enterprisesMapper.count(name, creditCode, operName, status);
  }

  @Override
  public List<Enterprise> listByUserId(Long userId, Integer offset, Integer size) {
    if (userId == null) {
      return Collections.emptyList();
    }
    return enterprisesMapper.listByUserId(userId, offset, size);
  }

  @Override
  public long countByUserId(Long userId) {
    if (userId == null) {
      return 0;
    }
    return enterprisesMapper.countByUserId(userId);
  }
}
