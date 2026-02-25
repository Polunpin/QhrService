package com.tencent.service.impl;

import com.tencent.dao.StaffsMapper;
import com.tencent.dto.StaffRequest;
import com.tencent.model.Staff;
import com.tencent.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffServiceImpl implements StaffService {

  private final StaffsMapper staffsMapper;

  public StaffServiceImpl(@Autowired StaffsMapper staffsMapper) {
    this.staffsMapper = staffsMapper;
  }

  @Override
  public Staff getById(Long id) {
    return staffsMapper.getById(id);
  }

  @Override
  public Staff getByMobile(String mobile) {
    return staffsMapper.getByMobile(mobile);
  }

  @Override
  public Long create(Staff staff) {
    staffsMapper.insert(staff);
    return staffsMapper.lastInsertId();
  }

  @Override
  public boolean update(Staff staff) {
    return staffsMapper.update(staff) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return staffsMapper.delete(id) > 0;
  }

  @Override
  public boolean updateStatus(Long id, Integer status) {
    return staffsMapper.updateStatus(id, status) > 0;
  }

  @Override
  public List<StaffRequest> list(String role, Integer status, String department, String mobile, Integer offset, Integer size) {
    return staffsMapper.list(role, status, department, mobile, offset, size);
  }

  @Override
  public long count(String role, Integer status, String department, String mobile) {
    return staffsMapper.count(role, status, department, mobile);
  }
}
