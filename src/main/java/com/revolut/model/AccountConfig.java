package com.revolut.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class AccountConfig {

  private final BigDecimal startingAmount;

  @JsonCreator
  public AccountConfig(@JsonProperty("amount") BigDecimal startingAmount) {
    this.startingAmount = startingAmount;
  }
}
