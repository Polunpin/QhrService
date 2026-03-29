package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.User;
import com.qhr.vo.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UsersMapper extends BaseMapper<User> {

  List<Users> list(@Param("offset") Integer offset,
                   @Param("size") Integer size);

}
