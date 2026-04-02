package com.qhr.dao;

import com.qhr.model.CustomServiceOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomServiceOrdersMapper {

  CustomServiceOrder getById(@Param("id") Long id);

  int insert(CustomServiceOrder order);

  int update(CustomServiceOrder order);

  int delete(@Param("id") Long id);

  int deleteByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

  int updateStage(@Param("id") Long id, @Param("currentStage") String currentStage);

  int updateServiceStatus(@Param("id") Long id, @Param("serviceStatus") String serviceStatus);

  int updateSettleStatus(@Param("id") Long id, @Param("settleStatus") String settleStatus);

  int assignStaff(@Param("id") Long id, @Param("staffId") Long staffId);

  List<CustomServiceOrder> list(@Param("enterpriseId") Long enterpriseId,
                                @Param("staffId") Long staffId,
                                @Param("serviceStatus") String serviceStatus,
                                @Param("currentStage") String currentStage,
                                @Param("settleStatus") String settleStatus,
                                @Param("offset") Integer offset,
                                @Param("size") Integer size);

  long count(@Param("enterpriseId") Long enterpriseId,
             @Param("staffId") Long staffId,
             @Param("serviceStatus") String serviceStatus,
             @Param("currentStage") String currentStage,
             @Param("settleStatus") String settleStatus);

  long lastInsertId();
}
