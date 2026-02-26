package com.tencent.service;

import com.tencent.model.MatchRecord;
import com.tencent.vo.MatchRecords;

import java.util.List;

public interface MatchRecordService {

  /** 根据ID查询匹配记录 */
  MatchRecord getById(Long id);

  /** 创建匹配记录并返回主键 */
  Long create(MatchRecord record);

  /** 更新匹配记录 */
  boolean update(MatchRecord record);

  /** 删除匹配记录 */
  boolean delete(Long id);

  /** 更新匹配记录状态 */
  boolean updateStatus(Long id, String status);

  /** 分页查询匹配记录列表 */
  List<MatchRecords> list(Long enterpriseId, Long intentionId, String status, Integer offset, Integer size);

  /** 统计匹配记录数量 */
  long count(Long enterpriseId, Long intentionId, String status);
}
