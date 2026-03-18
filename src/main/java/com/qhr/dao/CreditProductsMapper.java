package com.qhr.dao;

import com.qhr.vo.CreditProductStats;
import com.qhr.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CreditProductsMapper {

  Product getById(@Param("id") Long id);

  List<Product> getByIds(@Param("ids") List<Long> ids);

  int insert(Product product);

  int update(Product product);

  int delete(@Param("id") Long id);

  int updateStatus(@Param("id") Long id, @Param("status") Integer status);

  List<Product> list(@Param("status") Integer status,
                     @Param("productType") String productType,
                     @Param("bankName") String bankName,
                     @Param("offset") Integer offset,
                     @Param("size") Integer size);

  long count(@Param("status") Integer status,
             @Param("productType") String productType,
             @Param("bankName") String bankName);

  List<Product> findEligibleProducts(@Param("expectedAmount") BigDecimal expectedAmount,
                                     @Param("expectedTerm") Integer expectedTerm,
                                     @Param("productType") String productType,
                                     @Param("offset") Integer offset,
                                     @Param("size") Integer size);

  long countEligibleProducts(@Param("expectedAmount") BigDecimal expectedAmount,
                             @Param("expectedTerm") Integer expectedTerm,
                             @Param("productType") String productType);

  long lastInsertId();

  CreditProductStats getStats();
}
