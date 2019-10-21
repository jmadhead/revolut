package com.revolut.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TransferConfig {

  private final int toAccount;
  private final BigDecimal amount;

  @Builder
  @JsonCreator
  public TransferConfig(
      @JsonProperty("to") int toAccount,
      @JsonProperty("amount") BigDecimal amount) {
    this.toAccount = toAccount;
    this.amount = amount;
  }
}
