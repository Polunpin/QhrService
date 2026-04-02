package com.qhr.service;

import com.qhr.config.PageResult;
import com.qhr.model.Product;

import java.util.List;

public interface CreditProductService {

  /**
   * 分页查询产品列表
   */
  PageResult<Product> list(Integer offset, Integer size);

  /** 创建产品并返回主键 */
  Long create(Product product);

  /** 更新产品 */
  boolean update(Product product);

  /** 删除产品 */
  boolean delete(Long id);

  /**
   * 根据ID查询产品
   */
  Product getById(Long id);

  /**
   * 根据企业ID查询产品
   */
  List<Product> listEid(Long enterpriseId);
}
