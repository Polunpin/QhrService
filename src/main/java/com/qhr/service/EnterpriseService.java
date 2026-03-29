package com.qhr.service;

import com.qhr.config.PageResult;
import com.qhr.model.Enterprise;

public interface EnterpriseService {


  /**
   * 分页查询企业列表
   */
  PageResult<Enterprise> list(Integer offset, Integer size);


  /** 创建企业并返回主键 */
  Long create(Enterprise enterprise);

  /** 更新企业 */
  boolean update(Enterprise enterprise);

  /** 删除企业 */
  boolean delete(Long id);

  /**
   * 根据ID查询企业
   */
  Enterprise getById(Long id);

  /**
   * 分页查询用户关联企业
   */
  PageResult<Enterprise> listByUserId(Long userId, Integer offset, Integer size);
}
