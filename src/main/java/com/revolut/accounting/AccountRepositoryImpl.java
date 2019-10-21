package com.revolut.accounting;

import com.revolut.model.Account;
import com.revolut.model.AccountConfig;
import com.revolut.model.AccountsList;
import com.revolut.model.Transaction;
import com.revolut.model.TransferConfig;
import com.revolut.model.exceptions.AccountNotFound;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountRepositoryImpl implements AccountRepository {

  private final AtomicInteger accountIdProvider = new AtomicInteger(1);

  private final Map<Integer, Account> accounts = new ConcurrentHashMap<>();
  private final Account loanAccount;

  public AccountRepositoryImpl() {
    loanAccount = new AccountInMemory(accountIdProvider.getAndIncrement());
    accounts.put(loanAccount.getId(), loanAccount);
    Transaction loanAccountBalance = Transaction.builder()
        .from(0)
        .to(loanAccount.getId())
        .amount(new BigDecimal(Integer.MAX_VALUE))
        .build();
    loanAccount.add(loanAccountBalance);
  }

  @Override
  public Account get(int id) {
    Account account = accounts.get(id);
    if (account == null) {
      throw new AccountNotFound(id);
    }
    return account;
  }

  private void checkExists(int id) {
    get(id);
  }

  @Override
  public Account create(AccountConfig accountConfig) {
    Account account = new AccountInMemory(accountIdProvider.getAndIncrement());
    accounts.put(account.getId(), account);
    credit(account, accountConfig.getStartingAmount());
    return account;
  }

  @Override
  public Transaction transfer(Account from, TransferConfig config) {
    checkExists(from.getId());
    Account to = get(config.getToAccount());
    Transaction transaction = Transaction.builder()
        .from(from.getId())
        .to(config.getToAccount())
        .amount(config.getAmount())
        .build();

    from.add(transaction);
    to.add(transaction);
    return transaction;
  }

  @Override
  public AccountsList listAccounts() {
    ArrayList<Account> accountsCopy = new ArrayList<>(accounts.values());
    return new AccountsList(accountsCopy);
  }

  private void credit(Account toAccount, BigDecimal amount) {
    TransferConfig loanTransaction = TransferConfig.builder()
        .toAccount(toAccount.getId())
        .amount(amount)
        .build();
    transfer(loanAccount, loanTransaction);
  }

}
