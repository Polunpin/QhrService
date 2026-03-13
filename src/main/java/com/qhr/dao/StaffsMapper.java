package com.qhr.dao;

import com.qhr.vo.Staffs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StaffsMapper {

  com.qhr.model.Staff getById(@Param("id") Long id);

  com.qhr.model.Staff getByMobile(@Param("mobile") String mobile);

  int insert(com.qhr.model.Staff staff);

  int update(com.qhr.model.Staff staff);

  int delete(@Param("id") Long id);

  int updateStatus(@Param("id") Long id, @Param("status") Integer status);

  List<Staffs> list(@Param("role") String role,
                    @Param("status") Integer status,
                    @Param("department") String department,
                    @Param("mobile") String mobile,
                    @Param("offset") Integer offset,
                    @Param("size") Integer size);

  long count(@Param("role") String role,
             @Param("status") Integer status,
             @Param("department") String department,
             @Param("mobile") String mobile);

  long lastInsertId();
}
