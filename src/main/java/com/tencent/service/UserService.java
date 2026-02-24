package com.tencent.service;

import com.tencent.dto.UsersRequest;
import com.tencent.model.Enterprise;
import com.tencent.model.User;

import java.util.List;

public interface UserService {

  /** 根据ID查询用户 */
  User getById(Long id);

  /** 根据openid查询用户 */
  User getByOpenid(String openid);

  /** 创建用户并返回主键 */
  Long create(User user);

  /** 更新用户 */
  boolean update(User user);

  /** 删除用户 */
  boolean delete(Long id);

  /** 更新用户状态 */
  boolean updateStatus(Long id, Integer status);

  /** 分页查询用户列表 */
  List<UsersRequest> list(Integer status, String mobile, String realName, Integer offset, Integer size);

  /** 统计用户数量 */
  long count(Integer status, String mobile, String realName);

  /** 绑定用户与企业关系 */
  boolean bindEnterprise(Long userId, Long enterpriseId, String role);

  /** 解绑用户与企业关系 */
  boolean unbindEnterprise(Long userId, Long enterpriseId);

  /** 分页查询用户关联企业 */
  List<Enterprise> listEnterprises(Long userId, Integer offset, Integer size);

  /** 统计用户关联企业数量 */
  long countEnterprises(Long userId);

  /** 分页查询企业关联用户 */
  List<User> listUsersByEnterprise(Long enterpriseId, Integer offset, Integer size);

  /** 统计企业关联用户数量 */
  long countUsersByEnterprise(Long enterpriseId);
}
