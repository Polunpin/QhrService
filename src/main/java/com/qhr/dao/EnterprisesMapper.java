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

  int updateMatchStatus(@Param("id") Long id, @Param("matchStatus") String matchStatus);

  int updateProfileData(@Param("id") Long id, @Param("profileData") String profileData);

  List<Enterprise> list(@Param("matchStatus") String matchStatus,
                        @Param("industry") String industry,
                        @Param("regionCode") String regionCode,
                        @Param("offset") Integer offset,
                        @Param("size") Integer size);

  long count(@Param("matchStatus") String matchStatus,
             @Param("industry") String industry,
             @Param("regionCode") String regionCode);

  List<Enterprise> listByUserId(@Param("userId") Long userId,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long countByUserId(@Param("userId") Long userId);

  long lastInsertId();
}
