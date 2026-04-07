package com.qhr.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qhr.config.PageResult;
import com.qhr.dao.FinancingIntentionsMapper;
import com.qhr.dao.UserEnterpriseRelationMapper;
import com.qhr.model.FinancingIntention;
import com.qhr.model.UserEnterpriseRelation;
import com.qhr.service.HomeService;
import com.qhr.vo.*;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HomeServiceImpl implements HomeService {

    private final UserEnterpriseRelationMapper relationMapper;
    private final FinancingIntentionsMapper financingIntentionsMapper;

    public HomeServiceImpl(UserEnterpriseRelationMapper relationMapper,
                           FinancingIntentionsMapper financingIntentionsMapper) {
        this.relationMapper = relationMapper;
        this.financingIntentionsMapper = financingIntentionsMapper;
    }

    @Override
    public MiniHomeVO home(String openid, Long enterpriseId) {
        //todo
        MiniHomeDashboardVO miniHomeDashboardVO = new MiniHomeDashboardVO(
                "300-500万",
                new MiniHomeRadarVO(80, 60, 90, 100, 70),
                10,
                "4.2"
        );
        MiniHomeQuotaPredictionVO miniHomeQuotaPredictionVO = new MiniHomeQuotaPredictionVO("120-180万", 3, 60);

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
                        .eq(FinancingIntention::getEnterpriseId, enterpriseId)
        );
        //提额进度 todo
        String increaseProgress = "2/3";
        //订单记录 todo
        long orderCount = 3;
        return new MiniMineVO(enterpriseCount, measureCount, increaseProgress, orderCount);
    }
}
