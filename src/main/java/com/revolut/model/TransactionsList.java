package com.revolut.model;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class TransactionsList {

  private final List<Transaction> transactions;

  public TransactionsList(List<Transaction> transactions) {
    this.transactions = Collections.unmodifiableList(transactions);
  }
}
