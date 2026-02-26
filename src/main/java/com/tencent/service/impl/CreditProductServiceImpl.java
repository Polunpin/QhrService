package com.tencent.service.impl;

import com.tencent.dao.CreditProductsMapper;
import com.tencent.vo.CreditProductStats;
import com.tencent.model.CreditProduct;
import com.tencent.service.CreditProductService;
import com.tencent.vo.CreditProducts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CreditProductServiceImpl implements CreditProductService {

  private final CreditProductsMapper creditProductsMapper;

  public CreditProductServiceImpl(@Autowired CreditProductsMapper creditProductsMapper) {
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
  public List<CreditProducts> list(Integer status, String productType, String bankName, Integer offset, Integer size) {
    return creditProductsMapper.list(status, productType, bankName, offset, size);
  }

  @Override
  public long count(Integer status, String productType, String bankName) {
    return creditProductsMapper.count(status, productType, bankName);
  }

  @Override
  public List<CreditProduct> findEligibleProducts(BigDecimal expectedAmount,
                                                  Integer expectedTerm,
                                                  String regionCode,
                                                  String productType,
                                                  Integer offset,
                                                  Integer size) {
    return creditProductsMapper.findEligibleProducts(expectedAmount, expectedTerm, regionCode, productType, offset, size);
  }

  @Override
  public long countEligibleProducts(BigDecimal expectedAmount,
                                    Integer expectedTerm,
                                    String regionCode,
                                    String productType) {
    return creditProductsMapper.countEligibleProducts(expectedAmount, expectedTerm, regionCode, productType);
  }

    @Override
    public CreditProductStats getStats() {
      return creditProductsMapper.getStats();

    }
}
