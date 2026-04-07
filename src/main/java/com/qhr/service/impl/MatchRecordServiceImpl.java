package com.qhr.service.impl;

import com.qhr.dao.MatchRecordsMapper;
import com.qhr.model.MatchRecord;
import com.qhr.service.MatchRecordService;
import com.qhr.vo.MatchRecords;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class MatchRecordServiceImpl implements MatchRecordService {

    private final MatchRecordsMapper matchRecordsMapper;

    public MatchRecordServiceImpl(MatchRecordsMapper matchRecordsMapper) {
        this.matchRecordsMapper = matchRecordsMapper;
    }

    @Override
    public List<MatchRecords> list(String openid, Long enterpriseId) {
        //根据openid+企业ID查询匹配记录
        return matchRecordsMapper.list(openid, enterpriseId);
    }

    @Override
    public Long create(MatchRecord record) {
        matchRecordsMapper.insert(record);
        return record.getId();
    }

    @Override
    public boolean update(MatchRecord record) {
        return matchRecordsMapper.updateById(record) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return matchRecordsMapper.deleteById(id) > 0;
    }

    @Override
    public MatchRecord getById(Long id) {
        return matchRecordsMapper.selectById(id);
    }
}
