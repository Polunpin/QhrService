package com.qhr.dao;

import com.qhr.model.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EnterprisesMapper {

  Enterprise getById(@Param("id") Long id);

  int insert(Enterprise enterprise);

  int update(Enterprise enterprise);

  int delete(@Param("id") Long id);

  List<Enterprise> list(@Param("name") String name,
                        @Param("creditCode") String creditCode,
                        @Param("operName") String operName,
                        @Param("status") String status,
                        @Param("offset") Integer offset,
                        @Param("size") Integer size);

  long count(@Param("name") String name,
             @Param("creditCode") String creditCode,
             @Param("operName") String operName,
             @Param("status") String status);

  List<Enterprise> listByUserId(@Param("userId") Long userId,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long countByUserId(@Param("userId") Long userId);

  long lastInsertId();
}
