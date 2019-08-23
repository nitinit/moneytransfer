package com.usb.account;

import static com.usb.account.Account.*;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.actor.Status;

public class AccountActor extends AbstractLoggingActor {
    public static Props props(String accountId, double balance) {
        return Props.create(AccountActor.class, accountId, balance);
    }

    private String accountId;
    private double balance;

    public AccountActor(String accountId, double balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(Deposit.class, deposit -> {
                    balance += deposit.getAmount();
                    log().info("Deposit"+balance + ":Account Balance:"+balance);
                    getSender().tell(TransactionStatus.DONE, self());
                })
                .match(Withdraw.class, withDraw -> {
                    if (balance < withDraw.getAmount()) {
                        getSender().tell(new Status.Failure(AccountInsufficientBalance.init()), self());
                        return;
                    }
                    balance -= withDraw.getAmount();
                    log().info("Withdrawal balance"+withDraw.getAmount() + ":Account Balance:"+balance);
                    getSender().tell(TransactionStatus.DONE, self());
                })
                .match(GetBalance.class, view -> {
                    log().info("ViewBalanceResponse"+balance);
                    getSender().tell(ViewBalanceResponse.init(accountId, balance), self());
                })
                .match(ViewBalance.class, view -> {
                    log().info("ViewBalanceResponse"+balance);
                    getSender().tell(ViewBalanceResponse.init(accountId, balance, view.context()), self());
                })
                .matchAny(expr -> {
                    log().info("Wrong Message");
                    getSender().tell(TransactionStatus.FAILED, self());
                })
                .build();
    }
}
