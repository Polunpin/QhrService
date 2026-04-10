package com.qhr.service;

import com.qhr.enums.MeasureProgressStage;
import com.qhr.vo.MeasureProgressVO;
import io.smallrye.mutiny.Multi;

/**
 * 测额异步进度服务。
 * 负责记录阶段流转，并向前端提供快照和流式订阅能力。
 */
public interface MeasureProgressService {

    /**
     * 标记任务已提交，初始化第一条进度记录。
     */
    void markSubmitted(Long intentionId);

    /**
     * 写入指定阶段的进度日志。
     */
    void markStage(Long intentionId, MeasureProgressStage stage, String remark);

    /**
     * 写入失败阶段，供前端展示异常终态。
     */
    void markFailed(Long intentionId, String remark);

    /**
     * 获取某个测额任务的当前进度快照。
     */
    MeasureProgressVO getProgress(Long intentionId, String openid);

    /**
     * 以 SSE 形式持续推送进度变化。
     */
    Multi<MeasureProgressVO> stream(Long intentionId, String openid);
}
