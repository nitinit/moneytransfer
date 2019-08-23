package com.usb.account;

import java.util.HashMap;
import java.util.Optional;

import com.usb.account.Account.GetBalance;
import com.usb.transfer.Context;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class AccountManagerActor extends AbstractLoggingActor {
    /*
     * Initial balance map for account and balance. This map can provided at accountmanager actor initialization time by
     * passing as constructor argument. If it is being provided then accountManager actor will do create account actor
     * with initial balance from this map otherwise each account actor would be initialized with 200 EURO(see the
     * method @accountActor)
     */
    private HashMap<String, Optional<Double>> initialBalanceMap = new HashMap<>();

    /*
     * Map for a account given 'accountId' as key to his 'balance' as value
     */
    public AccountManagerActor(HashMap<String, Optional<Double>> initialBalances) {
        this.initialBalanceMap = initialBalances;
    }

    public static Props props(HashMap<Long, Optional<Long>> balances) {
        return Props.create(AccountManagerActor.class, balances);
    }

    public static Props props() {
        return Props.create(AccountManagerActor.class, new HashMap<String, Optional<Double>>());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Account.AccountCommand.class, this::sendCommand)
                .match(Account.GetBalance.class, getBalance -> {
                	getBalance(getBalance);
                })        		
                .matchEquals(Account.ViewAccountBalance.PRINT, cmd -> {
                    Context context = Context.init(cmd.toString(), sender(), 0);
                    getContext().getChildren().forEach(actorRef -> {
                        context.incrementCounter();
                        actorRef.tell(Account.ViewBalance.init(context), self());
                    });
                })
                .match(Account.ViewBalanceResponse.class, viewBalanceResponse -> {
                    log().info("View Account Balance:" + viewBalanceResponse.accountId() + ":"
                            + viewBalanceResponse.amount());
                    viewBalanceResponse.context().decrementCounter();
                    if (viewBalanceResponse.context().getCounter() == 0) {
                        viewBalanceResponse.context().getSenderActorRef().tell(Account.ViewAccountBalance.DONE, self());
                    }
                })
                .build();
    }
    
    private void getBalance(GetBalance getBalance) {
    	 ActorRef accountActor = accountActor(getBalance.getAccountId());
         accountActor.forward(getBalance, getContext());
    }

    private void sendCommand(Account.AccountCommand accountCommand) {
        ActorRef accountActor = accountActor(accountCommand.getAccountId());
        //FiniteDuration duration = Duration.create(1, TimeUnit.SECONDS);
        // Future<Object> result = ask(accountActor.get(), accountCommand, new Timeout(duration));
        // pipe(result, getContext().dispatcher()).to(sender());
        // PatternsCS.pipe(PatternsCS.ask(accountActor.get(), accountCommand, new Timeout(duration)),
        // getContext().dispatcher()).to(sender());
        accountActor.forward(accountCommand, getContext());
        // Here can also use tell - purpose to use different method(ask/pipe/tell/forward) and understand behaviour
    }

    private ActorRef accountActor(String accountId) {
        String actorName = getActorName(accountId);
        return getContext().findChild(actorName).map(actorRef -> Optional.of(actorRef))
                .orElseGet(() -> initialBalanceMap.getOrDefault(accountId, Optional.of(Double.valueOf(200)))
                        .map(balance -> createActor(accountId, balance, actorName)))
                .get();
    }

    private ActorRef createActor(String accountId, double balance, String actorName) {
        log().info("createActor for accountId:" + accountId);
        return getContext().actorOf(AccountActor.props(accountId, balance), actorName);
    }

    private String getActorName(String accountId) {
        return "account-" + accountId;
    }

}