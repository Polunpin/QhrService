package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhr.config.PageResult;
import com.qhr.dao.EnterprisesMapper;
import com.qhr.dao.FinancingIntentionsMapper;
import com.qhr.dao.MatchRecordsMapper;
import com.qhr.dao.UserEnterpriseRelationMapper;
import com.qhr.model.FinancingIntention;
import com.qhr.model.UserEnterpriseRelation;
import com.qhr.service.HomeService;
import com.qhr.vo.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class HomeServiceImpl implements HomeService {

    private static final int HOME_ENTERPRISE_LIMIT = 5;
    private static final int HOME_MEASURE_LIMIT = 5;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<List<MatchRecords.Product>> PRODUCT_LIST_TYPE = new TypeReference<>() {
    };

    private final EnterprisesMapper enterprisesMapper;
    private final UserEnterpriseRelationMapper relationMapper;
    private final FinancingIntentionsMapper financingIntentionsMapper;
    private final MatchRecordsMapper matchRecordsMapper;

    public HomeServiceImpl(EnterprisesMapper enterprisesMapper,
                           UserEnterpriseRelationMapper relationMapper,
                           FinancingIntentionsMapper financingIntentionsMapper,
                           MatchRecordsMapper matchRecordsMapper) {
        this.enterprisesMapper = enterprisesMapper;
        this.relationMapper = relationMapper;
        this.financingIntentionsMapper = financingIntentionsMapper;
        this.matchRecordsMapper = matchRecordsMapper;
    }

    @Override
    public MiniHomeVO home(String openid, Long enterpriseId) {
        MiniHomeDashboardVO miniHomeDashboardVO = new MiniHomeDashboardVO(
                "100-300万",
                new MiniHomeRadarVO(50, 10, 50, 10, 10),
                3,
                "4.2"
        );
        MiniHomeQuotaPredictionVO miniHomeQuotaPredictionVO = new MiniHomeQuotaPredictionVO("20-80万", 6, 60);

        return new MiniHomeVO(miniHomeDashboardVO, miniHomeQuotaPredictionVO);
    }

    @Override
    public PageResult<MiniMeasureItemVO> increase(String openid) {
        return null;
    }

    @Override
    public MiniMineVO mine(String openid, Long enterpriseId) {
        //用户绑定企业数量
        long enterpriseCount = relationMapper.selectCount(
                Wrappers.<UserEnterpriseRelation>lambdaQuery().eq(UserEnterpriseRelation::getUserOpenId, openid)
        );
        //测额纪录数量
        long measureCount = financingIntentionsMapper.selectCount(
                Wrappers.<FinancingIntention>lambdaQuery()
                        .eq(FinancingIntention::getUserOpenId, openid)
                        .eq(enterpriseId != null, FinancingIntention::getEnterpriseId, enterpriseId)
        );
        //提额进度 todo
        long increaseProgress = 6;
        //订单记录 todo
        long orderCount = 3;
        return new MiniMineVO(enterpriseCount, measureCount, increaseProgress, orderCount);
    }
}
