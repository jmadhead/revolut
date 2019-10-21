package com.revolut.model;

import java.math.BigDecimal;

public interface Account {

  void add(Transaction transaction);

  TransactionsList transactions();

  BigDecimal getBalance();

  int getId();
}
