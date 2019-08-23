package com.usb.transfer;

import akka.actor.ActorRef;

public class Context {
    private final Object idObject;
    private final ActorRef senderActorRef;
    private int counter;

    public Object getIdObject() {
        return idObject;
    }

    public ActorRef getSenderActorRef() {
        return senderActorRef;
    }

    public Context(Object idObject, ActorRef senderActorRef, int counter) {
        this.idObject = idObject;
        this.senderActorRef = senderActorRef;
        this.counter = counter;
    }

    public static Context init(Object idObject, ActorRef senderActorRef, int transactionCounter) {
        return new Context(idObject, senderActorRef, transactionCounter);
    }

    public int getCounter() {
        return counter;
    }

    public void incrementCounter() {
        this.counter++;
    }

    public void decrementCounter() {
        this.counter--;
    }


}