package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.CreditProductsMapper;
import com.qhr.model.Product;
import com.qhr.service.CreditProductService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CreditProductServiceImpl implements CreditProductService {

    private final CreditProductsMapper creditProductsMapper;

    public CreditProductServiceImpl(CreditProductsMapper creditProductsMapper) {
        this.creditProductsMapper = creditProductsMapper;
    }

    @Override
    public PageResult<Product> list(Integer offset, Integer size) {
        List<Product> products = creditProductsMapper.list(offset, size);
        Long count = creditProductsMapper.selectCount(Wrappers.lambdaQuery());
        return PageResult.of(products, count, offset, size);
    }

    @Override
    public List<Product> listEid(Long enterpriseId) {
        // 临时 mock：固定返回指定产品ID，后续再接入企业匹配逻辑
        List<Long> mockProductIds = List.of(1L, 2L, 3L, 10L);
        return creditProductsMapper.selectByIds(mockProductIds);
    }


    @Override
    public Product getById(Long id) {
        return creditProductsMapper.selectById(id);
    }

    @Override
    public Long create(Product product) {
        creditProductsMapper.insert(product);
        return product.getId();
    }

    @Override
    public boolean update(Product product) {
        return creditProductsMapper.updateById(product) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return creditProductsMapper.deleteById(id) > 0;
    }

}
