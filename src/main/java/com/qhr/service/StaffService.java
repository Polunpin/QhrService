package com.qhr.service;

import com.qhr.config.PageResult;
import com.qhr.model.Staff;
import com.qhr.vo.Staffs;

public interface StaffService {

    /**
     * 分页查询员工列表
     */
    PageResult<Staffs> list(Integer offset, Integer size);

  /** 根据ID查询员工 */
  Staff getById(Long id);

  /** 创建员工并返回主键 */
  Long create(Staff staff);

  /** 更新员工 */
  boolean update(Staff staff);

  /** 删除员工 */
  boolean delete(Long id);

}
