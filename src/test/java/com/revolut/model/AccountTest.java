package com.revolut.model;

import com.revolut.accounting.AccountInMemory;
import com.revolut.model.exceptions.InsufficientFunds;
import java.math.BigDecimal;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class AccountTest {

  private Account account;

  @Before
  public void init() {
    account = new AccountInMemory(1);
  }

  @Test
  public void addTest() {
    BigDecimal amount = new BigDecimal("1.0");
    Transaction transaction = Transaction.builder()
        .from(0)
        .to(account.getId())
        .amount(amount)
        .build();
    account.add(transaction);
    MatcherAssert.assertThat(account.getBalance(), Matchers.equalTo(amount));
  }

  @Test
  public void subTest() {
    BigDecimal amount = new BigDecimal("1.0");
    Transaction initialBalance = Transaction.builder()
        .from(0)
        .to(account.getId())
        .amount(amount)
        .build();
    account.add(initialBalance);

    BigDecimal subAmount = new BigDecimal("0.5");
    Transaction sub = Transaction.builder()
        .from(account.getId())
        .to(0)
        .amount(subAmount)
        .build();
    account.add(sub);

    BigDecimal expected = amount.subtract(subAmount);
    MatcherAssert.assertThat(account.getBalance(), Matchers.equalTo(expected));
  }

  @Test(expected = InsufficientFunds.class)
  public void balanceCantBeNegative() {
    BigDecimal amount = new BigDecimal("1.0");
    Transaction initialBalance = Transaction.builder()
        .from(0)
        .to(account.getId())
        .amount(amount)
        .build();
    account.add(initialBalance);

    BigDecimal subAmount = new BigDecimal("1.5");
    Transaction sub = Transaction.builder()
        .from(account.getId())
        .to(0)
        .amount(subAmount)
        .build();
    account.add(sub);
  }

}
