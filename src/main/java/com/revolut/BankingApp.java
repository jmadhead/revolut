package com.revolut;

import static akka.http.javadsl.server.PathMatchers.integerSegment;
import static akka.http.javadsl.server.PathMatchers.segment;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.ExceptionHandler;
import akka.http.javadsl.server.RejectionHandler;
import akka.http.javadsl.server.Route;
import akka.http.scaladsl.model.StatusCodes;
import com.revolut.accounting.AccountRepository;
import com.revolut.model.Account;
import com.revolut.model.AccountConfig;
import com.revolut.model.Transaction;
import com.revolut.model.TransferConfig;
import com.revolut.model.exceptions.AccountNotFound;
import com.revolut.model.exceptions.IllegalTransactionParameters;
import com.revolut.model.exceptions.InsufficientFunds;

public class BankingApp extends AllDirectives {

  private final AccountRepository rep;

  public BankingApp(AccountRepository rep) {
    this.rep = rep;
  }

  private Route accountsRoute() {
    return path("accounts", () -> concat(
        post(() -> entity(Jackson.unmarshaller(AccountConfig.class), conf -> {
          Account account = rep.create(conf);
          return complete(StatusCodes.Created(), account, Jackson.marshaller());
        })),
        get(() -> completeOK(rep.listAccounts(), Jackson.marshaller()))
    ));
  }

  private Route singleAccountRoute() {
    return path(segment("accounts").slash(integerSegment()), accountId -> concat(
        get(() -> completeOK(rep.get(accountId), Jackson.marshaller()))
    ));
  }

  private Route transactionsRoute() {
    return path(segment("accounts").slash(integerSegment()).slash("transactions"), accountId -> concat(
        get(() -> completeOK(rep.get(accountId).transactions(), Jackson.marshaller())),
        post(() -> entity(Jackson.unmarshaller(TransferConfig.class), conf -> {
          Account from = rep.get(accountId);
          Transaction transfer = rep.transfer(from, conf);
          return complete(StatusCodes.Created(), transfer, Jackson.marshaller());
        }))
    ));
  }

  public Route createRoute() {

    final ExceptionHandler customExceptionsHandler = ExceptionHandler.newBuilder()
        .match(AccountNotFound.class, exn -> complete(StatusCodes.NotFound(), exn.getMessage()))
        .match(InsufficientFunds.class, exn -> complete(StatusCodes.BadRequest(), exn.getMessage()))
        .match(IllegalTransactionParameters.class, exn -> complete(StatusCodes.BadRequest(), exn.getMessage()))
        .build();

    return concat(
        accountsRoute(),
        singleAccountRoute(),
        transactionsRoute()
    ).seal(
        RejectionHandler.defaultHandler(),
        customExceptionsHandler
    );
  }

}
