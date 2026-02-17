package com.tencent.service.impl;

import com.tencent.dao.ProductRedirectConfigsMapper;
import com.tencent.model.ProductRedirectConfig;
import com.tencent.service.ProductRedirectConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductRedirectConfigServiceImpl implements ProductRedirectConfigService {

  private final ProductRedirectConfigsMapper productRedirectConfigsMapper;

  public ProductRedirectConfigServiceImpl(@Autowired ProductRedirectConfigsMapper productRedirectConfigsMapper) {
    this.productRedirectConfigsMapper = productRedirectConfigsMapper;
  }

  @Override
  public ProductRedirectConfig getById(Long id) {
    return productRedirectConfigsMapper.getById(id);
  }

  @Override
  public Long create(ProductRedirectConfig config) {
    productRedirectConfigsMapper.insert(config);
    return productRedirectConfigsMapper.lastInsertId();
  }

  @Override
  public boolean update(ProductRedirectConfig config) {
    return productRedirectConfigsMapper.update(config) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return productRedirectConfigsMapper.delete(id) > 0;
  }

  @Override
  public boolean updateActive(Long id, Integer isActive) {
    return productRedirectConfigsMapper.updateActive(id, isActive) > 0;
  }

  @Override
  public boolean increaseClickCount(Long id) {
    return productRedirectConfigsMapper.increaseClickCount(id) > 0;
  }

  @Override
  public List<ProductRedirectConfig> list(Long productId,
                                          String configType,
                                          String externalUserId,
                                          Integer isActive,
                                          String targetName,
                                          Integer offset,
                                          Integer size) {
    return productRedirectConfigsMapper.list(productId, configType, externalUserId, isActive, targetName, offset, size);
  }

  @Override
  public long count(Long productId,
                    String configType,
                    String externalUserId,
                    Integer isActive,
                    String targetName) {
    return productRedirectConfigsMapper.count(productId, configType, externalUserId, isActive, targetName);
  }

  @Override
  public List<ProductRedirectConfig> listActiveByProductId(Long productId) {
    return productRedirectConfigsMapper.listActiveByProductId(productId);
  }
}
