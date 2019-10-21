package com.revolut.accounting;

import com.revolut.model.Account;
import com.revolut.model.Transaction;
import com.revolut.model.TransactionsList;
import com.revolut.model.exceptions.InsufficientFunds;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

public class AccountInMemory implements Account {

  @Getter
  private final int id;
  private final List<Transaction> transactions = new ArrayList<>();

  public AccountInMemory(int id) {
    this.id = id;
  }

  @Override
  public synchronized void add(Transaction transaction) {
    Objects.requireNonNull(transaction);
    checkCanApply(transaction);
    transactions.add(transaction);
  }

  private void checkCanApply(Transaction transaction) {
    boolean isExpense = transaction.getFrom() == getId();
    if (isExpense && isInsufficientFunds(transaction)) {
      throw new InsufficientFunds(id);
    }
  }

  private boolean isInsufficientFunds(Transaction transaction) {
    return transaction.getAmount().compareTo(getBalance()) > 0;
  }

  @Override
  public synchronized TransactionsList transactions() {
    return new TransactionsList(transactions);
  }

  @Override
  public synchronized BigDecimal getBalance() {
    return transactions.stream()
        .map(transaction -> {
          BigDecimal res;
          if (transaction.getFrom() == getId()) {
            res = transaction.getAmount().negate();
          } else {
            res = transaction.getAmount();
          }
          return res;
        })
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

}
