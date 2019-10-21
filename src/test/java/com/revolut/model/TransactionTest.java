package com.revolut.model;

import com.revolut.model.exceptions.IllegalTransactionParameters;
import java.math.BigDecimal;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class TransactionTest {

  @Test(expected = IllegalTransactionParameters.class)
  public void amountMustBePositive() {
    Transaction.builder()
        .from(1)
        .to(2)
        .amount(new BigDecimal("-1"))
        .build();
  }

  @Test(expected = IllegalTransactionParameters.class)
  public void amountMustBeNonzero() {
    Transaction.builder()
        .from(1)
        .to(2)
        .amount(BigDecimal.ZERO)
        .build();
  }

  @Test(expected = IllegalTransactionParameters.class)
  public void amountShouldBeNonnull() {
    Transaction.builder()
        .from(1)
        .to(0)
        .build();
  }

  @Test(expected = IllegalTransactionParameters.class)
  public void fromAndToMustDiffers() {
    Transaction.builder()
        .from(1)
        .to(1)
        .amount(BigDecimal.ONE)
        .build();
  }

  @Test
  public void positiveTest() {
    BigDecimal amount = BigDecimal.ONE;
    int from = 1;
    int to = 2;
    Transaction transaction = Transaction.builder()
        .from(from)
        .to(to)
        .amount(amount)
        .build();
    MatcherAssert.assertThat(transaction.getAmount(), CoreMatchers.equalTo(amount));
    MatcherAssert.assertThat(transaction.getFrom(), CoreMatchers.is(from));
    MatcherAssert.assertThat(transaction.getTo(), CoreMatchers.is(to));
  }

}
