package com.qhr.service;

import com.qhr.config.PageResult;
import com.qhr.model.User;
import com.qhr.vo.Users;

public interface UserService {

  /**
   * 分页查询用户列表
   */
  PageResult<Users> list(Integer offset, Integer size);

  /**
   * 绑定用户与企业关系
   */
  boolean bindEnterprise(String userOpenId, Long enterpriseId);


  /**
   * 创建用户并返回主键
   */
  Long create(String openid, String unionid);

  /**
   * 删除用户
   */
  boolean delete(Long id);

  /**
   * 更新用户
   */
  boolean update(User user);

  /**
   * 根据ID查询用户
   */
  User getById(Long id);

  /**
   * 根据openid查询用户
   */
  User getByOpenid(String openid);

}
