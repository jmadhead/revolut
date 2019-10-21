package com.revolut.accounting;

import com.revolut.model.Account;
import com.revolut.model.AccountConfig;
import com.revolut.model.AccountsList;
import com.revolut.model.Transaction;
import com.revolut.model.TransferConfig;

public interface AccountRepository {

  Account get(int id);

  Account create(AccountConfig accountConfig);

  Transaction transfer(Account from, TransferConfig config);

  AccountsList listAccounts();
}
