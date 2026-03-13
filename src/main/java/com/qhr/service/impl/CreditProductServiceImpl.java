package com.qhr.service.impl;

import com.qhr.dao.CreditProductsMapper;
import com.qhr.model.CreditProduct;
import com.qhr.service.CreditProductService;
import com.qhr.vo.CreditProductStats;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class CreditProductServiceImpl implements CreditProductService {

  private final CreditProductsMapper creditProductsMapper;

  public CreditProductServiceImpl(CreditProductsMapper creditProductsMapper) {
    this.creditProductsMapper = creditProductsMapper;
  }

  @Override
  public CreditProduct getById(Long id) {
    return creditProductsMapper.getById(id);
  }

  @Override
  public List<CreditProduct> getByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return creditProductsMapper.getByIds(ids);
  }

  @Override
  public Long create(CreditProduct product) {
    creditProductsMapper.insert(product);
    return creditProductsMapper.lastInsertId();
  }

  @Override
  public boolean update(CreditProduct product) {
    return creditProductsMapper.update(product) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return creditProductsMapper.delete(id) > 0;
  }

  @Override
  public boolean updateStatus(Long id, Integer status) {
    return creditProductsMapper.updateStatus(id, status) > 0;
  }

  @Override
  public List<CreditProduct> list(Integer status, String productType, String bankName, Integer offset, Integer size) {
    return creditProductsMapper.list(status, productType, bankName, offset, size);
  }

  @Override
  public long count(Integer status, String productType, String bankName) {
    return creditProductsMapper.count(status, productType, bankName);
  }

  @Override
  public List<CreditProduct> findEligibleProducts(BigDecimal expectedAmount,
                                                  Integer expectedTerm,
                                                  String productType,
                                                  Integer offset,
                                                  Integer size) {
    return creditProductsMapper.findEligibleProducts(expectedAmount, expectedTerm, productType, offset, size);
  }

  @Override
  public long countEligibleProducts(BigDecimal expectedAmount,
                                    Integer expectedTerm,
                                    String productType) {
    return creditProductsMapper.countEligibleProducts(expectedAmount, expectedTerm, productType);
  }

  @Override
  public CreditProductStats getStats() {
    return creditProductsMapper.getStats();
  }
}
