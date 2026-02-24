package com.tencent.dao;

import com.tencent.dto.UsersRequest;
import com.tencent.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UsersMapper {

  User getById(@Param("id") Long id);

  User getByOpenid(@Param("openid") String openid);

  int insert(User user);

  int update(User user);

  int delete(@Param("id") Long id);

  int updateStatus(@Param("id") Long id, @Param("status") Integer status);

  List<UsersRequest> list(@Param("status") Integer status,
                          @Param("mobile") String mobile,
                          @Param("realName") String realName,
                          @Param("offset") Integer offset,
                          @Param("size") Integer size);

  long count(@Param("status") Integer status,
             @Param("mobile") String mobile,
             @Param("realName") String realName);

  List<User> listByEnterpriseId(@Param("enterpriseId") Long enterpriseId,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long countByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

  long lastInsertId();
}
