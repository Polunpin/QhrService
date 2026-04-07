package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.*;
import com.qhr.model.Enterprise;
import com.qhr.model.FinancingIntention;
import com.qhr.model.MatchRecord;
import com.qhr.model.UserEnterpriseRelation;
import com.qhr.service.EnterpriseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

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
    //判断库中是否存在历史绑定信息
    if (enterprisesMapper.restoreByCreditCode(enterprise.getCreditCode()) > 0) {
      Enterprise restored = getByCreditCode(enterprise.getCreditCode());
      //更新企业基础信息
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
    //查询订单
//    List<CustomServiceOrder> orders = ordersMapper.list(id, null, null, null, null, null, null);
//    List<Long> orderIds = orders.stream()
//            .map(CustomServiceOrder::id)
//            .filter(Objects::nonNull)
//            .toList();
//    if (!orderIds.isEmpty()) {
//      //删除操作日志
//      statusLogsMapper.delete(
//              Wrappers.<StatusLog>lambdaQuery().in(StatusLog::getOrderId, orderIds)
//      );
//    }
    //删除订单
//    ordersMapper.deleteByEnterpriseId(id);
    //删除匹配记录
    matchRecordsMapper.delete(
            Wrappers.<MatchRecord>lambdaQuery().eq(MatchRecord::getEnterpriseId, id)
    );
    //删除融资需求
    financingIntentionsMapper.delete(
            Wrappers.<FinancingIntention>lambdaQuery().eq(FinancingIntention::getEnterpriseId, id)
    );
    //删除中间表：用户-企业
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
