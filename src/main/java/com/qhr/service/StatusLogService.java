package com.qhr.service;

import com.qhr.model.StatusLog;

import java.util.List;

public interface StatusLogService {

  /** 创建日志并返回主键 */
  Long create(StatusLog log);

  /** 根据ID查询日志 */
  StatusLog getById(Long id);

  /** 分页查询订单日志 */
  List<StatusLog> listByOrderId(Long orderId, Integer offset, Integer size);

  /** 统计订单日志数量 */
  long countByOrderId(Long orderId);
}
