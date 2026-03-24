package com.qhr.service;

import com.qhr.model.FinancingIntention;

import java.util.List;

public interface FinancingIntentionService {

  /** 根据ID查询融资需求 */
  FinancingIntention getById(Long id);

  /** 创建融资需求并返回主键 */
  Long create(FinancingIntention intention);

  /** 更新融资需求 */
  boolean update(FinancingIntention intention);

  /** 删除融资需求 */
  boolean delete(Long id);

  /** 分页查询融资需求列表 */
  List<FinancingIntention> list(Long enterpriseId, Integer offset, Integer size);

  /** 统计融资需求数量 */
  long count(Long enterpriseId);
}
