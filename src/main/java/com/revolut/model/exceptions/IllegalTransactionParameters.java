package com.revolut.model.exceptions;

public class IllegalTransactionParameters extends RuntimeException {

  public IllegalTransactionParameters(String message) {
    super(message);
  }
}
