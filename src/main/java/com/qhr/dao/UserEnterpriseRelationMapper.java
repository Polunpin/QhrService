package com.qhr.dao;

import com.qhr.model.UserEnterpriseRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserEnterpriseRelationMapper {

  UserEnterpriseRelation getByUserEnterprise(@Param("userOpenId") String userOpenId,
                                             @Param("enterpriseId") Long enterpriseId);

  int insert(UserEnterpriseRelation relation);

  int delete(@Param("id") Long id);

  int deleteByUserEnterprise(@Param("userId") Long userId,
                             @Param("enterpriseId") Long enterpriseId);

  List<UserEnterpriseRelation> listByUserId(@Param("userId") Long userId);

  List<UserEnterpriseRelation> listByEnterpriseId(@Param("enterpriseId") Long enterpriseId);
}
