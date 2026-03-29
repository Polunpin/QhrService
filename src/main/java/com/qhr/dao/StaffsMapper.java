package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.Staff;
import com.qhr.vo.Staffs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StaffsMapper extends BaseMapper<Staff> {

  List<Staffs> list(@Param("offset") Integer offset, @Param("size") Integer size);
}
