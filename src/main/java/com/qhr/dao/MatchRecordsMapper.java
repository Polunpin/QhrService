package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.MatchRecord;
import com.qhr.vo.MatchRecords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MatchRecordsMapper extends BaseMapper<MatchRecord> {

  List<MatchRecords> list(@Param("offset") Integer offset, @Param("size") Integer size);
}
