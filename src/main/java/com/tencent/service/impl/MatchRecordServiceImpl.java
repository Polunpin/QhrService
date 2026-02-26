package com.tencent.service.impl;

import com.tencent.dao.MatchRecordsMapper;
import com.tencent.model.MatchRecord;
import com.tencent.service.MatchRecordService;
import com.tencent.vo.MatchRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchRecordServiceImpl implements MatchRecordService {

    private final MatchRecordsMapper matchRecordsMapper;

    public MatchRecordServiceImpl(@Autowired MatchRecordsMapper matchRecordsMapper) {
        this.matchRecordsMapper = matchRecordsMapper;
    }

    @Override
    public MatchRecord getById(Long id) {
        return matchRecordsMapper.getById(id);
    }

    @Override
    public Long create(MatchRecord record) {
        matchRecordsMapper.insert(record);
        return matchRecordsMapper.lastInsertId();
    }

    @Override
    public boolean update(MatchRecord record) {
        return matchRecordsMapper.update(record) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return matchRecordsMapper.delete(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return matchRecordsMapper.updateStatus(id, status) > 0;
    }

    @Override
    public List<MatchRecords> list(Long enterpriseId, Long intentionId, String status, Integer offset, Integer size) {
        return matchRecordsMapper.list(enterpriseId, intentionId, status, offset, size);
    }

    @Override
    public long count(Long enterpriseId, Long intentionId, String status) {
        return matchRecordsMapper.count(enterpriseId, intentionId, status);
    }
}
