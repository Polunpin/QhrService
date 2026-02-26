package com.tencent.dao;

import com.tencent.model.MatchRecord;
import com.tencent.vo.MatchRecords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MatchRecordsMapper {

  MatchRecord getById(@Param("id") Long id);

  int insert(MatchRecord record);

  int update(MatchRecord record);

  int delete(@Param("id") Long id);

  int updateStatus(@Param("id") Long id, @Param("status") String status);

  List<MatchRecords> list(@Param("enterpriseId") Long enterpriseId,
                          @Param("intentionId") Long intentionId,
                          @Param("status") String status,
                          @Param("offset") Integer offset,
                          @Param("size") Integer size);

  long count(@Param("enterpriseId") Long enterpriseId,
             @Param("intentionId") Long intentionId,
             @Param("status") String status);

  long lastInsertId();
}
