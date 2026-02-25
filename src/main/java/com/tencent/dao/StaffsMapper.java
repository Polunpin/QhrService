package com.tencent.dao;

import com.tencent.dto.StaffRequest;
import com.tencent.model.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StaffsMapper {

  Staff getById(@Param("id") Long id);

  Staff getByMobile(@Param("mobile") String mobile);

  int insert(Staff staff);

  int update(Staff staff);

  int delete(@Param("id") Long id);

  int updateStatus(@Param("id") Long id, @Param("status") Integer status);

  List<StaffRequest> list(@Param("role") String role,
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
