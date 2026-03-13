package com.qhr.service;

import com.qhr.model.FinancingIntention;

import java.util.List;

public interface FinancingIntentionService {

  /** 根据ID查询融资意向 */
  FinancingIntention getById(Long id);

  /** 根据申请编号查询融资意向 */
  FinancingIntention getByApplicationNo(String applicationNo);

  /** 创建融资意向并返回主键 */
  Long create(FinancingIntention intention);

  /** 更新融资意向 */
  boolean update(FinancingIntention intention);

  /** 删除融资意向 */
  boolean delete(Long id);

  /** 更新融资意向状态 */
  boolean updateStatus(Long id, String status, String refusalReason);

  /** 更新意向目标产品 */
  boolean updateTargetProduct(Long id, Long targetProductId);

  /** 分页查询融资意向列表 */
  List<FinancingIntention> list(Long enterpriseId, Long userId, String status, Integer offset, Integer size);

  /** 统计融资意向数量 */
  long count(Long enterpriseId, Long userId, String status);
}
