package com.qhr.dao;

import com.qhr.model.FinancingIntention;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancingIntentionsMapper {

  FinancingIntention getById(@Param("id") Long id);

  int insert(FinancingIntention intention);

  int update(FinancingIntention intention);

  int delete(@Param("id") Long id);

  List<FinancingIntention> list(@Param("enterpriseId") Long enterpriseId,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long count(@Param("enterpriseId") Long enterpriseId);

  long lastInsertId();
}
