package com.revolut;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.revolut.accounting.AccountRepository;
import com.revolut.accounting.AccountRepositoryImpl;
import java.util.concurrent.CompletionStage;

public class BankingAppStarter {

  public static void main(String[] args) throws Exception {
    ActorSystem system = ActorSystem.create("routes");

    final Http http = Http.get(system);
    final ActorMaterializer materializer = ActorMaterializer.create(system);

    AccountRepository repository = new AccountRepositoryImpl();
    BankingApp app = new BankingApp(repository);

    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
    final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
        ConnectHttp.toHost("localhost", 8080), materializer);

    System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
    System.in.read();

    binding
        .thenCompose(ServerBinding::unbind)
        .thenAccept(unbound -> system.terminate());
  }

}
