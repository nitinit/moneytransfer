package com.usb;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import com.usb.account.AccountManagerActor;
import com.usb.account.Account.GetBalance;
import com.usb.account.Account.TransactionStatus;
import com.usb.account.Account.ViewBalanceResponse;
import com.usb.model.AcccountBalance;
import com.usb.model.Payment;
import com.usb.transfer.PaymentProcessingActor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import scala.concurrent.duration.Duration;

public class PaymentServer extends HttpApp {

  private ActorRef accountManageActor;
  private ActorRef paymentProcessingActor;

  Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));

  public PaymentServer(ActorRef accountManageActor, ActorRef paymentProcessingActor) {
	  this.accountManageActor = accountManageActor;
	  this.paymentProcessingActor = paymentProcessingActor;
  }

  @Override
  public Route routes() {
    return path("moneyTransfer", this::postUser)
            .orElse(path(segment("account").slash(segment()), id ->
                    route(getAccountBalance(id))));
  }

  private Route getAccountBalance(String id) {
	    return get(() -> {
	        @SuppressWarnings("unchecked")
			CompletionStage<ViewBalanceResponse> balanceResponse = PatternsCS.ask(accountManageActor, GetBalance.accountId(id.toString()), timeout)
	                .thenApply(obj -> (ViewBalanceResponse) obj);

	        return onSuccess(() -> balanceResponse, performed -> {
	          //if (performed.isPresent())
	            return complete(StatusCodes.OK, new AcccountBalance(id, performed.amount()), Jackson.marshaller());
	          //else
	          //  return complete(StatusCodes.NOT_FOUND);
	        });
	      });
  }
  
  private Route postUser() {
    return route(post(() -> entity(Jackson.unmarshaller(Payment.class), payment -> {    	
    	CompletionStage<TransactionStatus> txnStatus = PatternsCS.ask(paymentProcessingActor, payment, timeout)
          .thenApply(obj -> (TransactionStatus) obj);
    	
      return onSuccess(() -> txnStatus, performed -> {
        return complete(StatusCodes.CREATED, performed, Jackson.marshaller());
      });
    })));
  }

  public static void main(String[] args) throws Exception {
    ActorSystem system = ActorSystem.create("paymentServer");
    
    ActorRef accountManager = system.actorOf(AccountManagerActor.props(), "accountManager");
    ActorRef paymentProcessingActor = system.actorOf(PaymentProcessingActor.props(accountManager), "paymentProcessingManager");
    
    PaymentServer server = new PaymentServer(accountManager, paymentProcessingActor);
    server.startServer("localhost", 8080, system);
  }

}
