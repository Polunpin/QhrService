package com.qhr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MeasureSubmitRequest(
    String amountRange,
    Boolean property,
    Boolean propertyMortgage,
    Boolean spouseSupport,
    String taxAccount,
    String taxPassword,
    EnterprisePayload enterprise
) {
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
}
