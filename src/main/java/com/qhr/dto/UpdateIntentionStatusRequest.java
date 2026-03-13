package com.qhr.dto;

/**
 * 更新融资意向状态请求。
 */
public record UpdateIntentionStatusRequest(String status, String refusalReason) {
}
