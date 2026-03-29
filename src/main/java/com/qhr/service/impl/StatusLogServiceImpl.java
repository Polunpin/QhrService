package com.qhr.service.impl;

import com.qhr.dao.StatusLogsMapper;
import com.qhr.model.StatusLog;
import com.qhr.service.StatusLogService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StatusLogServiceImpl implements StatusLogService {

  private final StatusLogsMapper statusLogsMapper;

  public StatusLogServiceImpl(StatusLogsMapper statusLogsMapper) {
    this.statusLogsMapper = statusLogsMapper;
  }

  @Override
  public Long create(StatusLog log) {
    if (log == null) {
      return null;
    }
    statusLogsMapper.insert(log);
    return log.getId();
  }

  @Override
  public StatusLog getById(Long id) {
    return id == null ? null : statusLogsMapper.selectById(id);
  }

  @Override
  public List<StatusLog> listByOrderId(Long orderId, Integer offset, Integer size) {
    return statusLogsMapper.listByOrderId(orderId, offset, size);
  }

  @Override
  public long countByOrderId(Long orderId) {
    return statusLogsMapper.countByOrderId(orderId);
  }
}
