package com.tencent.service;

import com.tencent.dto.StaffRequest;
import com.tencent.model.Staff;

import java.util.List;

public interface StaffService {

  /** 根据ID查询员工 */
  Staff getById(Long id);

  /** 根据手机号查询员工 */
  Staff getByMobile(String mobile);

  /** 创建员工并返回主键 */
  Long create(Staff staff);

  /** 更新员工 */
  boolean update(Staff staff);

  /** 删除员工 */
  boolean delete(Long id);

  /** 更新员工状态 */
  boolean updateStatus(Long id, Integer status);

  /** 分页查询员工列表 */
  List<StaffRequest> list(String role, Integer status, String department, String mobile, Integer offset, Integer size);

  /** 统计员工数量 */
  long count(String role, Integer status, String department, String mobile);
}
