package com.qhr.dto;

@Deprecated
public record MeasureSubmitResponse(
    boolean fail,
    String why
) {
}
