package com.qhr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MeasureSubmitRequest(
    String amountRange,
    Boolean property,
    Boolean propertyMortgage,
    Boolean spouseSupport,
    String taxAccount,
    String taxPassword,
    String verifyCode,
    EnterprisePayload enterprise
) {
}
