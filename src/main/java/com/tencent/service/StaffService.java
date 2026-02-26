package com.tencent.service;

import com.tencent.vo.Staffs;

import java.util.List;

public interface StaffService {

  /** 根据ID查询员工 */
  com.tencent.model.Staff getById(Long id);

  /** 根据手机号查询员工 */
  com.tencent.model.Staff getByMobile(String mobile);

  /** 创建员工并返回主键 */
  Long create(com.tencent.model.Staff staff);

  /** 更新员工 */
  boolean update(com.tencent.model.Staff staff);

  /** 删除员工 */
  boolean delete(Long id);

  /** 更新员工状态 */
  boolean updateStatus(Long id, Integer status);

  /** 分页查询员工列表 */
  List<Staffs> list(String role, Integer status, String department, String mobile, Integer offset, Integer size);

  /** 统计员工数量 */
  long count(String role, Integer status, String department, String mobile);
}
