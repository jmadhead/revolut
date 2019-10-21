package com.revolut.model;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class AccountsList {

  private final List<Account> accounts;

  public AccountsList(List<Account> accounts) {
    this.accounts = Collections.unmodifiableList(accounts);
  }
}
