package com.qhr.service;

import com.qhr.model.Enterprise;

import java.util.List;

public interface EnterpriseService {

  /** 根据ID查询企业 */
  Enterprise getById(Long id);

  /** 创建企业并返回主键 */
  Long create(Enterprise enterprise);

  /** 更新企业 */
  boolean update(Enterprise enterprise);

  /** 删除企业 */
  boolean delete(Long id);

  /** 更新企业匹配状态 */
  boolean updateMatchStatus(Long id, String matchStatus);

  /** 更新企业画像数据 */
  boolean updateProfileData(Long id, String profileData);

  /** 分页查询企业列表 */
  List<Enterprise> list(String matchStatus, String industry, String regionCode, Integer offset, Integer size);

  /** 统计企业数量 */
  long count(String matchStatus, String industry, String regionCode);

  /** 分页查询用户关联企业 */
  List<Enterprise> listByUserId(Long userId, Integer offset, Integer size);

  /** 统计用户关联企业数量 */
  long countByUserId(Long userId);
}
