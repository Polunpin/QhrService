package com.qhr.dao;

import com.qhr.model.StatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatusLogsMapper {

  StatusLog getById(@Param("id") Long id);

  int insert(StatusLog log);

  List<StatusLog> listByOrderId(@Param("orderId") Long orderId,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long countByOrderId(@Param("orderId") Long orderId);

  long lastInsertId();
}
