package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.UserEnterpriseRelationMapper;
import com.qhr.dao.UsersMapper;
import com.qhr.model.User;
import com.qhr.model.UserEnterpriseRelation;
import com.qhr.service.UserService;
import com.qhr.vo.Users;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UserServiceImpl implements UserService {

  private final UsersMapper usersMapper;
  private final UserEnterpriseRelationMapper relationMapper;

  public UserServiceImpl(UsersMapper usersMapper,
                         UserEnterpriseRelationMapper relationMapper) {
    this.usersMapper = usersMapper;
    this.relationMapper = relationMapper;
  }


  @Override
  public PageResult<Users> list(Integer offset, Integer size) {
    List<Users> users = usersMapper.list(offset, size);
    Long count = usersMapper.selectCount(Wrappers.lambdaQuery());
    return PageResult.of(users, count, offset, size);
  }

  @Override
  public boolean bindEnterprise(String userOpenId, Long enterpriseId, String role) {
    UserEnterpriseRelation existing = relationMapper.getByUserEnterprise(userOpenId, enterpriseId);
    if (existing != null) {
      return true;
    }
    String safeRole = role == null || role.trim().isEmpty() ? "1" : role.trim();
    UserEnterpriseRelation relation = new UserEnterpriseRelation(null, enterpriseId, userOpenId, safeRole, null);
    return relationMapper.insert(relation) > 0;
  }


  @Override
  public Long create(String openid, String unionid) {

    User existing = getByOpenid(openid);
    if (existing != null) {
      return existing.getId();
    }

    User user = new User();
    user.setOpenid(openid);
    user.setUnionid(unionid);
    usersMapper.insert(user);
    return user.getId();
  }

  @Override
  public boolean update(User user) {
    return usersMapper.updateById(user) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return usersMapper.deleteById(id) > 0;
  }

  @Override
  public User getById(Long id) {
    return usersMapper.selectById(id);
  }

  @Override
  public User getByOpenid(String openid) {
    return usersMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getOpenid, openid));
  }

}
