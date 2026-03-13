package com.qhr.dto;

/**
 * 订单阶段推进请求。
 */
public record AdvanceStageRequest(String postStage,
                                  String serviceStatus,
                                  String remark,
                                  String operatorType,
                                  Long operatorId) {
}
