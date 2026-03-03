package com.tencent.service.impl;

import com.tencent.dao.FinancingIntentionsMapper;
import com.tencent.model.FinancingIntention;
import com.tencent.service.FinancingIntentionService;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class FinancingIntentionServiceImpl implements FinancingIntentionService {

  private final FinancingIntentionsMapper intentionsMapper;

  public FinancingIntentionServiceImpl(FinancingIntentionsMapper intentionsMapper) {
    this.intentionsMapper = intentionsMapper;
  }

  @Override
  public FinancingIntention getById(Long id) {
    return intentionsMapper.getById(id);
  }

  @Override
  public FinancingIntention getByApplicationNo(String applicationNo) {
    return intentionsMapper.getByApplicationNo(applicationNo);
  }

  @Override
  public Long create(FinancingIntention intention) {
    FinancingIntention toSave = intention;
    if (intention.applicationNo() == null || intention.applicationNo().trim().isEmpty()) {
      toSave = withApplicationNo(intention, generateApplicationNo());
    }
    intentionsMapper.insert(toSave);
    return intentionsMapper.lastInsertId();
  }

  @Override
  public boolean update(FinancingIntention intention) {
    return intentionsMapper.update(intention) > 0;
  }

  @Override
  public boolean delete(Long id) {
    return intentionsMapper.delete(id) > 0;
  }

  @Override
  public boolean updateStatus(Long id, String status, String refusalReason) {
    return intentionsMapper.updateStatus(id, status, refusalReason) > 0;
  }

  @Override
  public boolean updateTargetProduct(Long id, Long targetProductId) {
    return intentionsMapper.updateTargetProduct(id, targetProductId) > 0;
  }

  @Override
  public List<FinancingIntention> list(Long enterpriseId, Long userId, String status, Integer offset, Integer size) {
    return intentionsMapper.list(enterpriseId, userId, status, offset, size);
  }

  @Override
  public long count(Long enterpriseId, Long userId, String status) {
    return intentionsMapper.count(enterpriseId, userId, status);
  }

  private String generateApplicationNo() {
    // 业务申请编号: FI + 时间戳 + 随机4位
    String prefix = "FI";
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    int random = ThreadLocalRandom.current().nextInt(10000);
    return prefix + timestamp + String.format("%04d", random);
  }

  private FinancingIntention withApplicationNo(FinancingIntention source, String applicationNo) {
    return new FinancingIntention(source.id(),
        applicationNo,
        source.enterpriseId(),
        source.userId(),
        source.expectedAmount(),
        source.expectedTerm(),
        source.purpose(),
        source.repaymentSource(),
        source.guaranteeType(),
        source.targetProductId(),
        source.contactMobile(),
        source.status(),
        source.refusalReason(),
        source.urgencyLevel(),
        source.createdAt(),
        source.updatedAt());
  }
}
