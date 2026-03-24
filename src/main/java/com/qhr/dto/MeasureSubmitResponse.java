package com.qhr.dto;

public record MeasureSubmitResponse(
    boolean fail,
    String why
) {
}
