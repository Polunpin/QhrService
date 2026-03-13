package com.qhr.config;

import java.util.Collections;
import java.util.Map;

public record ApiResponse(int code, String errorMsg, Object data) {

    private static final Map<String, Object> EMPTY_DATA = Collections.emptyMap();

    public static ApiResponse ok() {
        return new ApiResponse(ApiCode.OK, "", EMPTY_DATA);
    }

    public static ApiResponse ok(Object data) {
        return new ApiResponse(ApiCode.OK, "", data == null ? EMPTY_DATA : data);
    }

    public static ApiResponse error(String errorMsg) {
        return new ApiResponse(ApiCode.INTERNAL_ERROR, errorMsg, EMPTY_DATA);
    }

    public static ApiResponse error(int code, String errorMsg) {
        return new ApiResponse(code, errorMsg, EMPTY_DATA);
    }
}
