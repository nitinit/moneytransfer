package com.usb.transfer;

import static com.usb.account.Account.*;

import akka.actor.*;

public class PaymentTransactionActor extends AbstractLoggingActor {
    public static Props props(ActorRef accountsManage) {
        return Props.create(PaymentTransactionActor.class, accountsManage);
    }

    private ActorRef accountManager;

    public PaymentTransactionActor(ActorRef accountManager) {
        this.accountManager = accountManager;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Transaction.class, transaction -> {
                    log().info("Transaction received:"+"fromAccount-"+transaction.fromAccount()+":toAccount-"+transaction.toAccount());
                    sendWithdrawalCommand(transaction, getSender());
                })
                .build();
    }

    private void sendWithdrawalCommand(Transaction transaction, ActorRef customer) {
        if (!transaction.shouldHavePositiveAmount()) {
            customer.tell(FailedTransactionDetail.init(transaction, new Status.Failure(NegativeTransferAmount.init())), self());
            return;
        }
        AccountCommand withdraw = Withdraw.init(transaction.fromAccount(), transaction.amount());
        accountManager.tell(withdraw, self());
        log().info("Withdrawal cmd sent");
        getContext().become(awaitWithdraw(transaction, customer));
    }

    private final AbstractActor.Receive awaitWithdraw(Transaction transaction, final ActorRef customer) {
        return receiveBuilder()
                .matchEquals(TransactionStatus.DONE, t -> {
                    log().info("Done-Withdraw");
                    accountManager.tell(Deposit.init(transaction.toAccount(), transaction.amount()),self());
                    getContext().become(awaitDeposit(transaction, customer));
                })
                .match(Status.Failure.class, failure -> {
                    customer.tell(FailedTransactionDetail.init(transaction, failure), self());
                    getContext().stop(self());
                })
                .build();
    }

    final AbstractActor.Receive awaitDeposit(Transaction transaction, final ActorRef customer) {
        return receiveBuilder()
                .matchEquals(TransactionStatus.DONE, t -> {
                    log().info("Done-deposit");
                    customer.tell(TransactionAck.init(transaction.transactionContext()), self());
                    getContext().stop(self());
                })
                .match(Status.Failure.class, failure -> {
                    customer.tell(FailedTransactionDetail.init(transaction, failure), self());
                    getContext().stop(self());
                })
                .build();
    }
}