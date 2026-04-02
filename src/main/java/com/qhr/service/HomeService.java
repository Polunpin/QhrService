package com.qhr.service;

import com.qhr.config.PageResult;
import com.qhr.vo.MiniHomeVO;
import com.qhr.vo.MiniMeasureItemVO;
import com.qhr.vo.MiniMineVO;

public interface HomeService {

    /**
     * 小程序首页查询。
     */
    MiniHomeVO home(String openid, Long enterpriseId);

    /**
     * 小程序提额查询。
     */
    PageResult<MiniMeasureItemVO> increase(String openid);

    /**
     * 小程序我的。
     */
    MiniMineVO mine(String openid, Long enterpriseId);
}
