package com.qhr.vo.match;

import lombok.Data;

/**
 * 结构化匹配原因，用于前端展示和后续提额诊断复用。
 */
@Data
public class ProductMatchReason {

    private String code;
    private String message;
    private String decisionNode;
    private String sourceField;
    private Object expected;
    private Object actual;
    private Boolean hardReject;
}
