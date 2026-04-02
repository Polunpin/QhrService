package com.qhr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qhr.model.UserEnterpriseRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserEnterpriseRelationMapper extends BaseMapper<UserEnterpriseRelation> {

  UserEnterpriseRelation getByUserEnterprise(@Param("userOpenId") String userOpenId,
                                             @Param("enterpriseId") Long enterpriseId);

  int deleteByUserEnterprise(@Param("userOpenId") String userOpenId,
                             @Param("enterpriseId") Long enterpriseId);

}
