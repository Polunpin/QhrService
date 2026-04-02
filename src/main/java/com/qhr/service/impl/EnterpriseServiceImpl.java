package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.*;
import com.qhr.model.*;
import com.qhr.service.EnterpriseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class EnterpriseServiceImpl implements EnterpriseService {

  private final EnterprisesMapper enterprisesMapper;
  private final FinancingIntentionsMapper financingIntentionsMapper;
  private final MatchRecordsMapper matchRecordsMapper;
  private final CustomServiceOrdersMapper ordersMapper;
  private final StatusLogsMapper statusLogsMapper;
  private final UserEnterpriseRelationMapper relationMapper;

  public EnterpriseServiceImpl(EnterprisesMapper enterprisesMapper,
                               FinancingIntentionsMapper financingIntentionsMapper,
                               MatchRecordsMapper matchRecordsMapper,
                               CustomServiceOrdersMapper ordersMapper,
                               StatusLogsMapper statusLogsMapper,
                               UserEnterpriseRelationMapper relationMapper) {
    this.enterprisesMapper = enterprisesMapper;
    this.financingIntentionsMapper = financingIntentionsMapper;
    this.matchRecordsMapper = matchRecordsMapper;
    this.ordersMapper = ordersMapper;
    this.statusLogsMapper = statusLogsMapper;
    this.relationMapper = relationMapper;
  }

  @Override
  public PageResult<Enterprise> list(Integer offset, Integer size) {
    List<Enterprise> enterprises = enterprisesMapper.list(offset, size);
    Long count = enterprisesMapper.selectCount(Wrappers.lambdaQuery());
    return PageResult.of(enterprises, count, offset, size);
  }

  public PageResult<Enterprise> listByOpenid(String openid, Integer offset, Integer size) {
    List<Enterprise> enterprises = enterprisesMapper.listByUserId(openid, offset, size);
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
    if (enterprise.getCreditCode() != null && enterprisesMapper.restoreByCreditCode(enterprise.getCreditCode()) > 0) {
      Enterprise restored = getByCreditCode(enterprise.getCreditCode());
      if (restored != null) {
        enterprise.setId(restored.getId());
        enterprisesMapper.updateById(enterprise);
        return restored.getId();
      }
    }
    enterprisesMapper.insert(enterprise);
    return enterprise.getId();
  }

  @Override
  public boolean update(Enterprise enterprise) {
    return enterprisesMapper.updateById(enterprise) > 0;
  }

  @Override
  @Transactional
  public boolean delete(Long id) {
    List<CustomServiceOrder> orders = ordersMapper.list(id, null, null, null, null, null, null);
    List<Long> orderIds = orders.stream()
            .map(CustomServiceOrder::id)
            .filter(Objects::nonNull)
            .toList();
    if (!orderIds.isEmpty()) {
      statusLogsMapper.delete(
              Wrappers.<StatusLog>lambdaQuery().in(StatusLog::getOrderId, orderIds)
      );
    }
    ordersMapper.deleteByEnterpriseId(id);
    matchRecordsMapper.delete(
            Wrappers.<MatchRecord>lambdaQuery().eq(MatchRecord::getEnterpriseId, id)
    );
    financingIntentionsMapper.delete(
            Wrappers.<FinancingIntention>lambdaQuery().eq(FinancingIntention::getEnterpriseId, id)
    );
    relationMapper.delete(
            Wrappers.<UserEnterpriseRelation>lambdaQuery().eq(UserEnterpriseRelation::getEnterpriseId, id)
    );
    return enterprisesMapper.deleteById(id) > 0;
  }

  @Override
  public Enterprise getById(Long id) {
    return enterprisesMapper.selectById(id);
  }

}
