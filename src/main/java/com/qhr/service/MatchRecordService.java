package com.qhr.service;

import com.qhr.config.PageResult;
import com.qhr.model.MatchRecord;
import com.qhr.vo.MatchRecords;

public interface MatchRecordService {

  /**
   * 分页查询匹配记录列表
   */
  PageResult<MatchRecords> list(Integer offset, Integer size);

  /** 创建匹配记录并返回主键 */
  Long create(MatchRecord record);

  /** 更新匹配记录 */
  boolean update(MatchRecord record);

  /** 删除匹配记录 */
  boolean delete(Long id);

  /**
   * 根据ID查询匹配记录
   */
  MatchRecord getById(Long id);
}
