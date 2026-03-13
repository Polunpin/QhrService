package com.qhr.dao;

import com.qhr.model.FinancingIntention;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancingIntentionsMapper {

  FinancingIntention getById(@Param("id") Long id);

  FinancingIntention getByApplicationNo(@Param("applicationNo") String applicationNo);

  int insert(FinancingIntention intention);

  int update(FinancingIntention intention);

  int delete(@Param("id") Long id);

  int updateStatus(@Param("id") Long id,
                   @Param("status") String status,
                   @Param("refusalReason") String refusalReason);

  int updateTargetProduct(@Param("id") Long id, @Param("targetProductId") Long targetProductId);

  List<FinancingIntention> list(@Param("enterpriseId") Long enterpriseId,
                                @Param("userId") Long userId,
                                @Param("status") String status,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long count(@Param("enterpriseId") Long enterpriseId,
             @Param("userId") Long userId,
             @Param("status") String status);

  long lastInsertId();
}
