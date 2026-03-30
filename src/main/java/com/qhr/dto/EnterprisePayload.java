package com.qhr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EnterprisePayload(
        @JsonProperty("Address")
        String address,
        @JsonProperty("CreditCode")
        String creditCode,
        @JsonProperty("KeyNo")
        String keyNo,
        @JsonProperty("Name")
        String name,
        @JsonProperty("No")
        String no,
        @JsonProperty("OperName")
        String operName,
        @JsonProperty("StartDate")
        String startDate,
        @JsonProperty("Status")
        String status
) {
}