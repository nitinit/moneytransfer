package com.usb.account;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AccountActorTest {
    static ActorSystem actorSystem;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create("AccountManageActorTest");
    }

    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

    @Test
    public void testAccountActorGetBalance(){
        new TestKit(actorSystem) {{
            final ActorRef probe = getRef();
            final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountActor.class, "alex", 1000d));

            accountManagerActor.tell(Account.GetBalance.accountId("alex"), probe);
            expectMsgEquals(Account.ViewBalanceResponse.init("alex", 1000d));

        }};
    }

    @Test
    public void testAccountActorDeposit(){
        new TestKit(actorSystem) {{
            final ActorRef probe = getRef();
            final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountActor.class, "alex", 1000d));

            accountManagerActor.tell(Account.Deposit.init("alex", 500d), probe);
            expectMsg(Account.TransactionStatus.DONE);

            accountManagerActor.tell(Account.GetBalance.accountId("alex"), probe);
            expectMsgEquals(Account.ViewBalanceResponse.init("alex", 1500d));
        }};
    }

    @Test
    public void testAccountActorWithdrawal(){
        new TestKit(actorSystem) {{
            final ActorRef probe = getRef();
            final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountActor.class, "alex", 1000d));

            accountManagerActor.tell(Account.Withdraw.init("alex", 500d), probe);
            expectMsg(Account.TransactionStatus.DONE);

            accountManagerActor.tell(Account.GetBalance.accountId("alex"), probe);
            expectMsgEquals(Account.ViewBalanceResponse.init("alex", 500d));
        }};
    }


    @Test
    public void testAccountActorWithdrawal_InsufficientBalance(){
        new TestKit(actorSystem) {{
            final ActorRef probe = getRef();
            final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountActor.class, "alex", 1000d));

            accountManagerActor.tell(Account.Withdraw.init("alex", 1500d), probe);

            Status.Failure failure = expectMsgClass(Status.Failure.class);
            Assert.assertTrue(failure.cause() instanceof Account.AccountInsufficientBalance);
        }};
    }

}
