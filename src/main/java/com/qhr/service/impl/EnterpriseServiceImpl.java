package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.EnterprisesMapper;
import com.qhr.model.Enterprise;
import com.qhr.service.EnterpriseService;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class EnterpriseServiceImpl implements EnterpriseService {

  private final EnterprisesMapper enterprisesMapper;

  public EnterpriseServiceImpl(EnterprisesMapper enterprisesMapper) {
    this.enterprisesMapper = enterprisesMapper;
  }

  @Override
  public PageResult<Enterprise> list(Integer offset, Integer size) {
    List<Enterprise> enterprises = enterprisesMapper.list(offset, size);
    Long count = enterprisesMapper.selectCount(Wrappers.lambdaQuery());
    return PageResult.of(enterprises, count, offset, size);
  }

  public PageResult<Enterprise> listByUserId(Long userId, Integer offset, Integer size) {
    List<Enterprise> enterprises = enterprisesMapper.listByUserId(userId, offset, size);
    Long count = enterprisesMapper.selectCount(Wrappers.lambdaQuery());
    return PageResult.of(enterprises, count, offset, size);
  }

  @Override
  public Enterprise getByCreditCode(String creditCode) {
    return enterprisesMapper.selectOne(
            Wrappers.<Enterprise>lambdaQuery().eq(Enterprise::getCreditCode, creditCode)
    );
  }

  @Override
  public Long create(Enterprise enterprise) {
    enterprisesMapper.insert(enterprise);
    return enterprise.getId();
  }

  @Override
  public boolean update(Enterprise enterprise) {
    return enterprisesMapper.updateById(enterprise) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return enterprisesMapper.deleteById(id) > 0;
  }

  @Override
  public Enterprise getById(Long id) {
    return enterprisesMapper.selectById(id);
  }

}
