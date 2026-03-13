package com.qhr.service.impl;

import com.qhr.dao.StaffsMapper;
import com.qhr.service.StaffService;
import com.qhr.vo.Staffs;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StaffServiceImpl implements StaffService {

  private final StaffsMapper staffsMapper;

  public StaffServiceImpl(StaffsMapper staffsMapper) {
      this.staffsMapper = staffsMapper;
  }

  @Override
  public com.qhr.model.Staff getById(Long id) {
    return staffsMapper.getById(id);
  }

  @Override
  public com.qhr.model.Staff getByMobile(String mobile) {
    return staffsMapper.getByMobile(mobile);
  }

  @Override
  public Long create(com.qhr.model.Staff staff) {
    staffsMapper.insert(staff);
    return staffsMapper.lastInsertId();
  }

  @Override
  public boolean update(com.qhr.model.Staff staff) {
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
  public List<Staffs> list(String role, Integer status, String department, String mobile, Integer offset, Integer size) {
    return staffsMapper.list(role, status, department, mobile, offset, size);
  }

  @Override
  public long count(String role, Integer status, String department, String mobile) {
    return staffsMapper.count(role, status, department, mobile);
  }
}
