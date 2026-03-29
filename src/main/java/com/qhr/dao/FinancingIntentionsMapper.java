package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.FinancingIntention;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancingIntentionsMapper extends BaseMapper<FinancingIntention> {

  List<FinancingIntention> list(@Param("enterpriseId") Long enterpriseId,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long count(@Param("enterpriseId") Long enterpriseId);
}
