package com.qhr.service;

import com.qhr.vo.CreditProductStats;
import com.qhr.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface CreditProductService {

  /** 根据ID查询产品 */
  Product getById(Long id);

  /** 根据ID列表批量查询产品 */
  List<Product> getByIds(List<Long> ids);

  /** 创建产品并返回主键 */
  Long create(Product product);

  /** 更新产品 */
  boolean update(Product product);

  /** 删除产品 */
  boolean delete(Long id);

  /** 更新产品状态 */
  boolean updateStatus(Long id, Integer status);

  /** 分页查询产品列表 */
  List<Product> list(Integer status, String productType, String bankName, Integer offset, Integer size);

  /** 统计产品数量 */
  long count(Integer status, String productType, String bankName);

  /** 分页匹配符合条件的产品 */
  List<Product> findEligibleProducts(BigDecimal expectedAmount,
                                     Integer expectedTerm,
                                     String productType,
                                     Integer offset,
                                     Integer size);

  /** 统计符合条件的产品数量 */
  long countEligibleProducts(BigDecimal expectedAmount,
                             Integer expectedTerm,
                             String productType);

    /** 产品统计查询 */
    CreditProductStats getStats();

}
