package com.tencent.service.impl;

import com.tencent.dao.EnterprisesMapper;
import com.tencent.model.Enterprise;
import com.tencent.service.EnterpriseService;
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
  public boolean updateMatchStatus(Long id, String matchStatus) {
    return enterprisesMapper.updateMatchStatus(id, matchStatus) > 0;
  }

  @Override
  public boolean updateProfileData(Long id, String profileData) {
    return enterprisesMapper.updateProfileData(id, profileData) > 0;
  }

  @Override
  public List<Enterprise> list(String matchStatus, String industry, String regionCode, Integer offset, Integer size) {
    return enterprisesMapper.list(matchStatus, industry, regionCode, offset, size);
  }

  @Override
  public long count(String matchStatus, String industry, String regionCode) {
    return enterprisesMapper.count(matchStatus, industry, regionCode);
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
