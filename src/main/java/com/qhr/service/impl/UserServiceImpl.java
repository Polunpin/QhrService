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
  public void bindEnterprise(String userOpenId, Long enterpriseId) {
    //查询绑定是否存在
    UserEnterpriseRelation existing = relationMapper.getByUserEnterprise(userOpenId, enterpriseId);
    if (existing != null) {
      return;
    }
    //重新绑定-激活旧绑定信息
    if (relationMapper.restoreByUserEnterprise(userOpenId, enterpriseId) > 0) {
      return;
    }
    UserEnterpriseRelation relation = new UserEnterpriseRelation(enterpriseId, userOpenId);
    relationMapper.insert(relation);
  }


  @Override
  public Long create(String openid, String unionid) {

    User existing = getByOpenid(openid);
    if (existing != null) {
      return existing.getId();
    }

    User user = new User(openid, unionid);
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
