package com.qhr.service;

import com.qhr.model.MatchRecord;
import com.qhr.vo.MatchRecords;

import java.util.List;

public interface MatchRecordService {

  /**
   * 分页查询匹配记录列表
   */
  List<MatchRecords> list(String openid, Long enterpriseId);

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
