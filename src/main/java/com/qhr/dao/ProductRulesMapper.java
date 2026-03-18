package com.qhr.dao;

import com.qhr.model.ProductRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductRulesMapper {

  ProductRule getById(@Param("id") Long id);

  ProductRule getByProductIdAndRuleVersion(@Param("productId") Long productId,
                                           @Param("ruleVersion") Integer ruleVersion);

  int insert(ProductRule productRule);

  int update(ProductRule productRule);

  int delete(@Param("id") Long id);

  List<ProductRule> list();

  List<ProductRule> list(@Param("productId") Long productId,
                         @Param("ruleVersion") Integer ruleVersion,
                         @Param("ruleName") String ruleName,
                         @Param("isActive") Integer isActive,
                         @Param("offset") Integer offset,
                         @Param("size") Integer size);

  long count(@Param("productId") Long productId,
             @Param("ruleVersion") Integer ruleVersion,
             @Param("ruleName") String ruleName,
             @Param("isActive") Integer isActive);

  long lastInsertId();
}
