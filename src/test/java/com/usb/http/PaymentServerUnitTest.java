package com.usb.http;

import org.junit.Test;

import com.usb.PaymentServer;
import com.usb.account.AccountManagerActor;
import com.usb.transfer.PaymentProcessingActor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;

public class PaymentServerUnitTest extends JUnitRouteTest {

  ActorSystem system = ActorSystem.create("helloAkkaHttpServer");

  ActorRef accountManager = system.actorOf(AccountManagerActor.props(), "accountManager");
  ActorRef paymentProcessingActor = system.actorOf(PaymentProcessingActor.props(accountManager), "paymentProcessingManager");
  
  TestRoute appRoute = testRoute(new PaymentServer(accountManager, paymentProcessingActor).routes());

  @Test
  public void whenRequest_thenActorResponds() {

    appRoute.run(HttpRequest.GET("/account/Alisa"))
            .assertEntity(alisaAccount(200.0))
            .assertStatusCode(200);
    
    appRoute.run(HttpRequest.POST("/moneyTransfer")
            .withEntity(HttpEntities.create(ContentTypes.APPLICATION_JSON, paymentTransfer())))
            .assertStatusCode(201);
    
    appRoute.run(HttpRequest.GET("/account/Alisa"))
		    .assertEntity(alisaAccount(300.0))
		    .assertStatusCode(200);
  }

  private String alisaAccount(double amount) {
    return "{\"accountId\":\"Alisa\",\"balance\":"+amount+"}";
  }

  private String paymentTransfer() {
    return "{\"id\":42,\"from\":\"Zaphod\", \"to\":\"Alisa\", \"amount\":\"100\"}";
  }

}
