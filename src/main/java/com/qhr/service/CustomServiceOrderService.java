package com.qhr.service;

import com.qhr.model.CustomServiceOrder;

import java.util.List;

public interface CustomServiceOrderService {

  /** 根据ID查询订单 */
  CustomServiceOrder getById(Long id);

  /** 创建订单并返回主键 */
  Long create(CustomServiceOrder order);

  /** 更新订单 */
  boolean update(CustomServiceOrder order);

  /** 删除订单 */
  boolean delete(Long id);

  /** 指派订单负责人 */
  boolean assignStaff(Long id, Long staffId);

  /** 更新订单服务状态 */
  boolean updateServiceStatus(Long id, String serviceStatus);

  /** 更新订单结算状态 */
  boolean updateSettleStatus(Long id, String settleStatus);

  /** 推进订单阶段并写入日志 */
  boolean advanceStage(Long id, String postStage, String serviceStatus,
                       String remark, String operatorType, Long operatorId);

  /** 分页查询订单列表 */
  List<CustomServiceOrder> list(Long enterpriseId, Long staffId,
                                String serviceStatus, String currentStage, String settleStatus,
                                Integer offset, Integer size);

  /** 统计订单数量 */
  long count(Long enterpriseId, Long staffId,
             String serviceStatus, String currentStage, String settleStatus);
}
