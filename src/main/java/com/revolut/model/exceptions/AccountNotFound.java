package com.revolut.model.exceptions;

public class AccountNotFound extends RuntimeException {

  public AccountNotFound(int id) {
    super(String.format("Account %d not exists", id));
  }
}
