package com.revolut.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.revolut.model.exceptions.IllegalTransactionParameters;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Transaction {

  private static final AtomicInteger ID_PROVIDER = new AtomicInteger(1);

  private final int id;
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonFormat(shape = Shape.STRING)
  private final LocalDateTime date;
  private final int from;
  private final int to;
  private final BigDecimal amount;

  @Builder
  private Transaction(int from, int to, BigDecimal amount) {

    if (from == to) {
      throw new IllegalTransactionParameters("Cant transfer to same account");
    }

    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalTransactionParameters("Amount must be positive");
    }

    this.id = ID_PROVIDER.incrementAndGet();
    this.date = LocalDateTime.now();
    this.from = from;
    this.to = to;
    this.amount = amount;
  }

}
