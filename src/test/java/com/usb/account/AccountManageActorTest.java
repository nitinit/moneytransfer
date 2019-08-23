package com.usb.account;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.usb.account.Account.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class AccountManageActorTest {
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
    public void testAccountManageActorGetBalance(){
        new TestKit(actorSystem) {{
            final ActorRef probe = getRef();
            Map<String, Optional<Double>> balances = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(toMap(identity(), j -> Optional.of(Double.valueOf(j)*100)));

            final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountManagerActor.class, balances));

            accountManagerActor.tell(GetBalance.accountId("10"), probe);
            expectMsgEquals(ViewBalanceResponse.init("10", 1000d));

            accountManagerActor.tell(GetBalance.accountId("20"), probe);
            expectMsgEquals(ViewBalanceResponse.init("20", 2000d));
        }};
    }

    @Test
    public void testAccountManageDeposit() throws InterruptedException{
        new TestKit(actorSystem) {{
            final ActorRef probe = getRef();

            Map<String, Optional<Double>> balances = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(toMap(identity(), j -> Optional.of(Double.valueOf(j)*50)));

            final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountManagerActor.class, balances));

            accountManagerActor.tell(GetBalance.accountId("10"), probe);
            Thread.sleep(5000);
            expectMsgEquals(ViewBalanceResponse.init("10", 500d));

            accountManagerActor.tell(GetBalance.accountId("20"), probe);
            expectMsgEquals(ViewBalanceResponse.init("20", 1000d));

            accountManagerActor.tell(Deposit.init("10", 250), probe);
            expectMsgEquals(TransactionStatus.DONE);
            accountManagerActor.tell(GetBalance.accountId("10"), probe);
            expectMsgEquals(ViewBalanceResponse.init("10", 750d));

            accountManagerActor.tell(Deposit.init("20", 150), probe);
            expectMsgEquals(TransactionStatus.DONE);
            accountManagerActor.tell(GetBalance.accountId("20"), probe);
            expectMsgEquals(ViewBalanceResponse.init("20", 1150d));
        }};
    }

    @Test
    public void testAccountManageWithdrawal(){
        new TestKit(actorSystem) {{
            final ActorRef probe = getRef();

            Map<String, Optional<Double>> balances = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(toMap(identity(), j -> Optional.of(Double.valueOf(j)*50)));

            final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountManagerActor.class, balances));

            accountManagerActor.tell(GetBalance.accountId("10"), probe);
            expectMsgEquals(ViewBalanceResponse.init("10", 500d));

            accountManagerActor.tell(GetBalance.accountId("20"), probe);
            expectMsgEquals(ViewBalanceResponse.init("20", 1000d));

            accountManagerActor.tell(Withdraw.init("10", 250), probe);
            expectMsgEquals(TransactionStatus.DONE);
            accountManagerActor.tell(GetBalance.accountId("10"), probe);
            expectMsgEquals(ViewBalanceResponse.init("10", 250d));

            accountManagerActor.tell(Withdraw.init("20", 150), probe);
            expectMsgEquals(TransactionStatus.DONE);
            accountManagerActor.tell(GetBalance.accountId("20"), probe);
            expectMsgEquals(ViewBalanceResponse.init("20", 850d));
        }};
    }
}