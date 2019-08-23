package com.usb.transfer;

import com.usb.account.Account;
import com.usb.account.Account.TransactionStatus;
import com.usb.model.Payment;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class PaymentProcessingActor extends AbstractLoggingActor {

    public static Props props(ActorRef accountsManage) {
        return Props.create(PaymentProcessingActor.class, accountsManage);
    }

    private ActorRef accountManager;

    public PaymentProcessingActor(ActorRef accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Payment.class, payment -> {
                	
                        Context context = Context.init(payment.getId(), sender(), 0);
                        Transaction paymentTransaction = Transaction.init(payment.getFrom(), payment.getTo(), payment.getAmount(), context);
                        ActorRef paymentTransactionActor = getContext().actorOf(PaymentTransactionActor.props(accountManager), paymentTransaction.toString());
                        paymentTransactionActor.tell(paymentTransaction, self());
                 }).match(Account.FailedTransactionDetail.class, failedTransactionDetail -> {
                    log().info("Transaction Failed:"+failedTransactionDetail);
                    // TODO If needs to retry
                }).match(Account.TransactionAck.class, transactionAck -> {
                    if (transactionAck.getContext().getCounter() == 0) {
                        transactionAck.getContext().getSenderActorRef().tell(TransactionStatus.DONE, self());
                    }
                    log().info("Account transaction completed successfully:"+transactionAck.getContext().getIdObject());
                })
                .build();
    }
}