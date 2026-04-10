package com.qhr.dto;

import com.qhr.vo.MeasureProgressVO;

/**
 * 测额提交接口的同步响应。
 * 返回任务主键、当前进度快照，以及前端后续拉取/订阅进度的入口。
 */
public record MeasureSubmitResult(
        String status,
        Long intentionId,
        MeasureProgressVO progress,
        String progressPath,
        String progressStreamPath
) {
}
