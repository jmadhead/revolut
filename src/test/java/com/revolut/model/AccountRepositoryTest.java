package com.revolut.model;

import com.revolut.accounting.AccountInMemory;
import com.revolut.accounting.AccountRepository;
import com.revolut.accounting.AccountRepositoryImpl;
import com.revolut.model.exceptions.AccountNotFound;
import java.math.BigDecimal;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

public class AccountRepositoryTest {

  private AccountRepository rep;
  private BigDecimal creditAmount;
  private AccountConfig accountConfig;
  private Account account;

  @Before
  public void init() {
    rep = new AccountRepositoryImpl();
    creditAmount = new BigDecimal("10");
    accountConfig = new AccountConfig(creditAmount);
    account = rep.create(accountConfig);
  }

  @Test
  public void creditTest() {
    MatcherAssert.assertThat(account.getBalance(), CoreMatchers.equalTo(creditAmount));
  }

  @Test
  public void transferTest() {
    Account accountTo = rep.create(accountConfig);

    TransferConfig transfer = TransferConfig.builder()
        .toAccount(accountTo.getId())
        .amount(creditAmount)
        .build();
    rep.transfer(account, transfer);
    MatcherAssert.assertThat(account.getBalance(), CoreMatchers.equalTo(BigDecimal.ZERO));
    BigDecimal expected = creditAmount.add(creditAmount);
    MatcherAssert.assertThat(accountTo.getBalance(), CoreMatchers.equalTo(expected));
  }

  @Test(expected = AccountNotFound.class)
  public void cantTransferToNonexistentAccount() {
    Account nonExistent = new AccountInMemory(-1);
    TransferConfig transfer = TransferConfig.builder()
        .toAccount(nonExistent.getId())
        .amount(new BigDecimal("1"))
        .build();

    rep.transfer(account, transfer);
  }

  @Test(expected = AccountNotFound.class)
  public void cantTransferFromNonexistentAccount() {
    Account nonExistent = new AccountInMemory(-1);
    TransferConfig transfer = TransferConfig.builder()
        .toAccount(account.getId())
        .amount(new BigDecimal("1"))
        .build();

    rep.transfer(nonExistent, transfer);
  }

}
