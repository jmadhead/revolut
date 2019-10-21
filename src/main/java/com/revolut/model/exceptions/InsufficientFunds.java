package com.revolut.model.exceptions;

public class InsufficientFunds extends RuntimeException {

  public InsufficientFunds(int accountId) {
    super(String.format("Insufficient funds on account %d", accountId));
  }
}
