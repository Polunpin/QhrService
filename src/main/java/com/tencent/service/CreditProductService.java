package com.tencent.service;

import com.tencent.vo.CreditProductStats;
import com.tencent.model.CreditProduct;
import com.tencent.vo.CreditProducts;

import java.math.BigDecimal;
import java.util.List;

public interface CreditProductService {

  /** 根据ID查询产品 */
  CreditProduct getById(Long id);

  /** 根据ID列表批量查询产品 */
  List<CreditProduct> getByIds(List<Long> ids);

  /** 创建产品并返回主键 */
  Long create(CreditProduct product);

  /** 更新产品 */
  boolean update(CreditProduct product);

  /** 删除产品 */
  boolean delete(Long id);

  /** 更新产品状态 */
  boolean updateStatus(Long id, Integer status);

  /** 分页查询产品列表 */
  List<CreditProducts> list(Integer status, String productType, String bankName, Integer offset, Integer size);

  /** 统计产品数量 */
  long count(Integer status, String productType, String bankName);

  /** 分页匹配符合条件的产品 */
  List<CreditProduct> findEligibleProducts(BigDecimal expectedAmount,
                                           Integer expectedTerm,
                                           String regionCode,
                                           String productType,
                                           Integer offset,
                                           Integer size);

  /** 统计符合条件的产品数量 */
  long countEligibleProducts(BigDecimal expectedAmount,
                             Integer expectedTerm,
                             String regionCode,
                             String productType);

    /** 产品统计查询 */
    CreditProductStats getStats();

}
