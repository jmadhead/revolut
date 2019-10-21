package com.revolut;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.revolut.accounting.AccountRepository;
import com.revolut.accounting.AccountRepositoryImpl;
import com.revolut.model.AccountConfig;
import com.revolut.model.TransferConfig;
import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;


public class FunctionalApiTest extends JUnitRouteTest {
  private TestRoute appRoute;
  private AccountRepository repository;
  private BankingApp app;
  private JsonProvider jsonProvider;

  @Before
  public void init() {
    repository = new AccountRepositoryImpl();
    app = new BankingApp(repository);
    appRoute = testRoute(app.createRoute());
    jsonProvider = Configuration.defaultConfiguration().jsonProvider();
  }

  @Test
  public void hasLoanAccount() {
    String accountsListJson = appRoute.run(HttpRequest.GET("/accounts"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();

    Object accountsList = jsonProvider.parse(accountsListJson);
    assertThat(accountsList, hasJsonPath("$.accounts", hasSize(1)));
    assertThat(accountsList, hasJsonPath("$.accounts[0].id", is(1)));
    assertThat(accountsList, hasJsonPath("$.accounts[0].balance", equalTo(Integer.MAX_VALUE)));
  }

  @Test
  public void createAccountTest() throws JsonProcessingException {
    BigDecimal amount = new BigDecimal("20");
    String newAccountJson = createAccount(amount);

    Object newAccount = jsonProvider.parse(newAccountJson);
    assertThat(newAccount, hasJsonPath("$.id", is(2)));
    assertThat(newAccount, hasJsonPath("$.balance", equalTo(20)));

    String accountsListJson = appRoute.run(HttpRequest.GET("/accounts"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();

    Object accountsList = jsonProvider.parse(accountsListJson);
    assertThat(accountsList, hasJsonPath("$.accounts", hasSize(2)));
  }

  @Test
  public void getSingleAccount() {
    String accountsListJson = appRoute.run(HttpRequest.GET("/accounts/1"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();

    Object accountsList = jsonProvider.parse(accountsListJson);
    assertThat(accountsList, hasJsonPath("$.id", is(1)));
    assertThat(accountsList, hasJsonPath("$.balance", equalTo(Integer.MAX_VALUE)));
  }

  @Test
  public void newAccountHasOnlyOneTransaction() throws JsonProcessingException {
    createAccount(new BigDecimal("20"));

    String transactionsListJson = appRoute.run(HttpRequest.GET("/accounts/2/transactions"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();

    Object transactionsList = jsonProvider.parse(transactionsListJson);
    assertThat(transactionsList, hasJsonPath("$.transactions", is(hasSize(1))));
    assertThat(transactionsList, hasJsonPath("$.transactions[0].date", is(Matchers.notNullValue())));
    assertThat(transactionsList, hasJsonPath("$.transactions[0].from", is(1)));
    assertThat(transactionsList, hasJsonPath("$.transactions[0].to", is(2)));
    assertThat(transactionsList, hasJsonPath("$.transactions[0].amount", is(20)));
  }

  @Test
  public void transferTest() throws JsonProcessingException {
    BigDecimal amount = new BigDecimal("20");
    createAccount(amount);
    createAccount(amount);

    String acc1Json = appRoute.run(HttpRequest.GET("/accounts/2"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();
    String acc2Json = appRoute.run(HttpRequest.GET("/accounts/3"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();

    Object acc1 = jsonProvider.parse(acc1Json);
    Object acc2 = jsonProvider.parse(acc2Json);

    assertThat(acc1, hasJsonPath("$.balance", is(20)));
    assertThat(acc2, hasJsonPath("$.balance", is(20)));

    TransferConfig transferConfig = TransferConfig.builder()
        .toAccount(3)
        .amount(BigDecimal.TEN)
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(transferConfig);

    String transactionStr = appRoute.run(HttpRequest.POST("/accounts/2/transactions")
        .withEntity(ContentTypes.APPLICATION_JSON, body))
        .assertStatusCode(StatusCodes.CREATED)
        .entityString();

    Object transferTransaction = jsonProvider.parse(transactionStr);
    assertThat(transferTransaction, hasJsonPath("$.date", is(Matchers.notNullValue())));
    assertThat(transferTransaction, hasJsonPath("$.from", is(2)));
    assertThat(transferTransaction, hasJsonPath("$.to", is(3)));
    assertThat(transferTransaction, hasJsonPath("$.amount", is(10)));

    acc1Json = appRoute.run(HttpRequest.GET("/accounts/2"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();
    acc2Json = appRoute.run(HttpRequest.GET("/accounts/3"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();

    acc1 = jsonProvider.parse(acc1Json);
    acc2 = jsonProvider.parse(acc2Json);

    assertThat(acc1, hasJsonPath("$.balance", is(10)));
    assertThat(acc2, hasJsonPath("$.balance", is(30)));

    String acc1TransactionsJson = appRoute.run(HttpRequest.GET("/accounts/2/transactions"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();
    String acc2TransactionsJson = appRoute.run(HttpRequest.GET("/accounts/3/transactions"))
        .assertStatusCode(StatusCodes.OK)
        .entityString();

    Object acc1Transactions = jsonProvider.parse(acc1TransactionsJson);
    Object acc2Transactions = jsonProvider.parse(acc2TransactionsJson);

    assertThat(acc1Transactions, hasJsonPath("$.transactions", is(hasSize(2))));
    assertThat(acc1Transactions, hasJsonPath("$.transactions[1].date", is(Matchers.notNullValue())));
    assertThat(acc1Transactions, hasJsonPath("$.transactions[1].from", is(2)));
    assertThat(acc1Transactions, hasJsonPath("$.transactions[1].to", is(3)));
    assertThat(acc1Transactions, hasJsonPath("$.transactions[1].amount", is(10)));

    assertThat(acc2Transactions, hasJsonPath("$.transactions", is(hasSize(2))));
    assertThat(acc2Transactions, hasJsonPath("$.transactions[1].date", is(Matchers.notNullValue())));
    assertThat(acc2Transactions, hasJsonPath("$.transactions[1].from", is(2)));
    assertThat(acc2Transactions, hasJsonPath("$.transactions[1].to", is(3)));
    assertThat(acc2Transactions, hasJsonPath("$.transactions[1].amount", is(10)));
  }

  private String createAccount(BigDecimal amount) throws JsonProcessingException {
    AccountConfig accountConfig = new AccountConfig(amount);

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(accountConfig);

    return appRoute.run(HttpRequest.POST("/accounts")
        .withEntity(ContentTypes.APPLICATION_JSON, body))
        .assertStatusCode(StatusCodes.CREATED)
        .entityString();
  }

  @Test
  public void get404IfAccountWasNotFound_getAccount() {
    appRoute.run(HttpRequest.GET("/accounts/2"))
        .assertStatusCode(StatusCodes.NOT_FOUND);
  }

  @Test
  public void get404IfAccountWasNotFound_getAccountTransactions() {
    appRoute.run(HttpRequest.GET("/accounts/2/transactions"))
        .assertStatusCode(StatusCodes.NOT_FOUND);
  }

  @Test
  public void get404IfAccountWasNotFound_createTransfer() throws JsonProcessingException {
    TransferConfig transferConfig = TransferConfig.builder()
        .toAccount(3)
        .amount(BigDecimal.TEN)
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(transferConfig);

    appRoute.run(HttpRequest.POST("/accounts/2/transactions")
        .withEntity(ContentTypes.APPLICATION_JSON, body))
        .assertStatusCode(StatusCodes.NOT_FOUND);
  }

  @Test
  public void get400IfInsufficientFounds() throws JsonProcessingException {
    createAccount(new BigDecimal("20"));
    createAccount(new BigDecimal("20"));
    TransferConfig transferConfig = TransferConfig.builder()
        .toAccount(3)
        .amount(new BigDecimal("100"))
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(transferConfig);

    appRoute.run(HttpRequest.POST("/accounts/2/transactions")
        .withEntity(ContentTypes.APPLICATION_JSON, body))
        .assertStatusCode(StatusCodes.BAD_REQUEST);
  }

  @Test
  public void get400IfTransferringToSameAccount() throws JsonProcessingException {
    createAccount(new BigDecimal("20"));
    TransferConfig transferConfig = TransferConfig.builder()
        .toAccount(2)
        .amount(new BigDecimal("100"))
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(transferConfig);

    appRoute.run(HttpRequest.POST("/accounts/2/transactions")
        .withEntity(ContentTypes.APPLICATION_JSON, body))
        .assertStatusCode(StatusCodes.BAD_REQUEST);
  }

  @Test
  public void get400IfTransferringNegativeAmount() throws JsonProcessingException {
    createAccount(new BigDecimal("20"));
    createAccount(new BigDecimal("20"));
    TransferConfig transferConfig = TransferConfig.builder()
        .toAccount(3)
        .amount(new BigDecimal("-1"))
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(transferConfig);

    appRoute.run(HttpRequest.POST("/accounts/2/transactions")
        .withEntity(ContentTypes.APPLICATION_JSON, body))
        .assertStatusCode(StatusCodes.BAD_REQUEST);
  }

  @Test
  public void get400IfTransferringZeroAmount() throws JsonProcessingException {
    createAccount(new BigDecimal("20"));
    createAccount(new BigDecimal("20"));
    TransferConfig transferConfig = TransferConfig.builder()
        .toAccount(3)
        .amount(new BigDecimal("0"))
        .build();

    ObjectMapper objectMapper = new ObjectMapper();
    String body = objectMapper.writeValueAsString(transferConfig);

    appRoute.run(HttpRequest.POST("/accounts/2/transactions")
        .withEntity(ContentTypes.APPLICATION_JSON, body))
        .assertStatusCode(StatusCodes.BAD_REQUEST);
  }

}
