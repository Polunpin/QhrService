package com.qhr.service.impl;

import com.qhr.dao.CustomServiceOrdersMapper;
import com.qhr.dao.StatusLogsMapper;
import com.qhr.model.CustomServiceOrder;
import com.qhr.model.StatusLog;
import com.qhr.service.CustomServiceOrderService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CustomServiceOrderServiceImpl implements CustomServiceOrderService {

  private final CustomServiceOrdersMapper ordersMapper;
  private final StatusLogsMapper statusLogsMapper;

  public CustomServiceOrderServiceImpl(CustomServiceOrdersMapper ordersMapper,
                                       StatusLogsMapper statusLogsMapper) {
    this.ordersMapper = ordersMapper;
    this.statusLogsMapper = statusLogsMapper;
  }

  @Override
  public CustomServiceOrder getById(Long id) {
    return ordersMapper.getById(id);
  }

  @Override
  public Long create(CustomServiceOrder order) {
    ordersMapper.insert(order);
    return ordersMapper.lastInsertId();
  }

  @Override
  public boolean update(CustomServiceOrder order) {
    return ordersMapper.update(order) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return ordersMapper.delete(id) > 0;
  }

  @Override
  public boolean assignStaff(Long id, Long staffId) {
    return ordersMapper.assignStaff(id, staffId) > 0;
  }

  @Override
  public boolean updateServiceStatus(Long id, String serviceStatus) {
    return ordersMapper.updateServiceStatus(id, serviceStatus) > 0;
  }

  @Override
  public boolean updateSettleStatus(Long id, String settleStatus) {
    return ordersMapper.updateSettleStatus(id, settleStatus) > 0;
  }

  @Override
  @Transactional
  public boolean advanceStage(Long id, String postStage, String serviceStatus,
                              String remark, String operatorType, Long operatorId) {
    CustomServiceOrder existing = ordersMapper.getById(id);
    if (existing == null) {
      return false;
    }
    // 阶段推进并写入流转日志，保证业务可追溯
    boolean updated = ordersMapper.updateStage(id, postStage) > 0;
    if (serviceStatus != null && !serviceStatus.trim().isEmpty()) {
      ordersMapper.updateServiceStatus(id, serviceStatus);
    }
    String safeOperatorType = operatorType == null || operatorType.trim().isEmpty() ? "system" : operatorType.trim();
    StatusLog log = new StatusLog(null,
        id,
        safeOperatorType,
        operatorId,
        existing.currentStage(),
        postStage,
        remark,
        null);
    statusLogsMapper.insert(log);
    return updated;
  }

  @Override
  public List<CustomServiceOrder> list(Long enterpriseId, Long staffId,
                                       String serviceStatus, String currentStage, String settleStatus,
                                       Integer offset, Integer size) {
    return ordersMapper.list(enterpriseId, staffId, serviceStatus, currentStage, settleStatus, offset, size);
  }

  @Override
  public long count(Long enterpriseId, Long staffId,
                    String serviceStatus, String currentStage, String settleStatus) {
    return ordersMapper.count(enterpriseId, staffId, serviceStatus, currentStage, settleStatus);
  }
}
