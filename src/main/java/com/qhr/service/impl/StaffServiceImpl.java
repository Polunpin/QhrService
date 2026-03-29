package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.StaffsMapper;
import com.qhr.model.Staff;
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
  public PageResult<Staffs> list(Integer offset, Integer size) {
    List<Staffs> staffs = staffsMapper.list(offset, size);
    Long count = staffsMapper.selectCount(Wrappers.lambdaQuery());
    return PageResult.of(staffs, count, offset, size);
  }

  @Override
  public Staff getById(Long id) {
    return staffsMapper.selectById(id);
  }

  @Override
  public Long create(Staff staff) {
    staffsMapper.insert(staff);
    return staff.getId();
  }

  @Override
  public boolean update(Staff staff) {
    return staffsMapper.updateById(staff) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return staffsMapper.deleteById(id) > 0;
  }
}
