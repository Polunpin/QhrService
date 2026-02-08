package com.tencent.service.impl;

import com.tencent.dao.CountersMapper;
import com.tencent.model.Counter;
import com.tencent.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CounterServiceImpl implements CounterService {

  final CountersMapper countersMapper;

  public CounterServiceImpl(@Autowired CountersMapper countersMapper) {
    this.countersMapper = countersMapper;
  }

  @Override
  public Optional<Counter> getCounter(Integer id) {
    return Optional.ofNullable(countersMapper.getCounter(id));
  }

  @Override
  public void upsertCount(Counter counter) {
    countersMapper.upsertCount(counter);
  }

  @Override
  public void clearCount(Integer id) {
    countersMapper.clearCount(id);
  }
}
