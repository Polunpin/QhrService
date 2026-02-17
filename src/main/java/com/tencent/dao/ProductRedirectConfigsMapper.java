package com.tencent.dao;

import com.tencent.model.ProductRedirectConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductRedirectConfigsMapper {

  ProductRedirectConfig getById(@Param("id") Long id);

  int insert(ProductRedirectConfig config);

  int update(ProductRedirectConfig config);

  int delete(@Param("id") Long id);

  int updateActive(@Param("id") Long id, @Param("isActive") Integer isActive);

  int increaseClickCount(@Param("id") Long id);

  List<ProductRedirectConfig> list(@Param("productId") Long productId,
                                   @Param("configType") String configType,
                                   @Param("externalUserId") String externalUserId,
                                   @Param("isActive") Integer isActive,
                                   @Param("targetName") String targetName,
                                   @Param("offset") Integer offset,
                                   @Param("size") Integer size);

  long count(@Param("productId") Long productId,
             @Param("configType") String configType,
             @Param("externalUserId") String externalUserId,
             @Param("isActive") Integer isActive,
             @Param("targetName") String targetName);

  List<ProductRedirectConfig> listActiveByProductId(@Param("productId") Long productId);

  long lastInsertId();
}
