package com.tencent.service.impl;

import com.tencent.dao.EnterprisesMapper;
import com.tencent.dao.UserEnterpriseRelationMapper;
import com.tencent.dao.UsersMapper;
import com.tencent.dto.UsersRequest;
import com.tencent.model.Enterprise;
import com.tencent.model.User;
import com.tencent.model.UserEnterpriseRelation;
import com.tencent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

  private final UsersMapper usersMapper;
  private final UserEnterpriseRelationMapper relationMapper;
  private final EnterprisesMapper enterprisesMapper;

  public UserServiceImpl(@Autowired UsersMapper usersMapper,
                         @Autowired UserEnterpriseRelationMapper relationMapper,
                         @Autowired EnterprisesMapper enterprisesMapper) {
    this.usersMapper = usersMapper;
    this.relationMapper = relationMapper;
    this.enterprisesMapper = enterprisesMapper;
  }

  @Override
  public User getById(Long id) {
    return usersMapper.getById(id);
  }

  @Override
  public User getByOpenid(String openid) {
    return usersMapper.getByOpenid(openid);
  }

  @Override
  public Long create(User user) {
    usersMapper.insert(user);
    return usersMapper.lastInsertId();
  }

  @Override
  public boolean update(User user) {
    return usersMapper.update(user) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return usersMapper.delete(id) > 0;
  }

  @Override
  public boolean updateStatus(Long id, Integer status) {
    return usersMapper.updateStatus(id, status) > 0;
  }

  @Override
  public List<UsersRequest> list(Integer status, String mobile, String realName, Integer offset, Integer size) {
    return usersMapper.list(status, mobile, realName, offset, size);
  }

  @Override
  public long count(Integer status, String mobile, String realName) {
    return usersMapper.count(status, mobile, realName);
  }

  @Override
  public boolean bindEnterprise(Long userId, Long enterpriseId, String role) {
    UserEnterpriseRelation existing = relationMapper.getByUserEnterprise(userId, enterpriseId);
    if (existing != null) {
      return true;
    }
    String safeRole = role == null || role.trim().isEmpty() ? "owner" : role.trim();
    UserEnterpriseRelation relation = new UserEnterpriseRelation(null, enterpriseId, userId, safeRole, null);
    return relationMapper.insert(relation) > 0;
  }

  @Override
  public boolean unbindEnterprise(Long userId, Long enterpriseId) {
    return relationMapper.deleteByUserEnterprise(userId, enterpriseId) > 0;
  }

  @Override
  public List<Enterprise> listEnterprises(Long userId, Integer offset, Integer size) {
    if (userId == null) {
      return Collections.emptyList();
    }
    return enterprisesMapper.listByUserId(userId, offset, size);
  }

  @Override
  public long countEnterprises(Long userId) {
    if (userId == null) {
      return 0;
    }
    return enterprisesMapper.countByUserId(userId);
  }

  @Override
  public List<User> listUsersByEnterprise(Long enterpriseId, Integer offset, Integer size) {
    if (enterpriseId == null) {
      return Collections.emptyList();
    }
    return usersMapper.listByEnterpriseId(enterpriseId, offset, size);
  }

  @Override
  public long countUsersByEnterprise(Long enterpriseId) {
    if (enterpriseId == null) {
      return 0;
    }
    return usersMapper.countByEnterpriseId(enterpriseId);
  }
}
