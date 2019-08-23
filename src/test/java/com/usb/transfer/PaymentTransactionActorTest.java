package com.usb.transfer;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.usb.account.Account;
import com.usb.account.AccountManagerActor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class PaymentTransactionActorTest {
    static ActorSystem actorSystem;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create("PaymentTransactionActorTest");
    }

    @AfterClass
    public static void tearDown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

    @Test
    public void testMoneyTransfer() throws InterruptedException {
        new TestKit(actorSystem) {
            {
                final ActorRef probe = getRef();
                Map<String, Optional<Double>> balances = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(toMap(identity(), j -> Optional.of(Double.valueOf(j)*100)));

                final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountManagerActor.class, balances));
                final ActorRef moneyTransferActor = actorSystem.actorOf(Props.create(PaymentTransactionActor.class, accountManagerActor));

                accountManagerActor.tell(Account.GetBalance.accountId("10"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("10", 1000d));
                accountManagerActor.tell(Account.GetBalance.accountId("20"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("20", 2000d));

                moneyTransferActor.tell(Transaction.init("10", "20", 180, Context.init("transactionId-1", probe, 1)), probe);
                Account.TransactionAck ack = expectMsgClass(Account.TransactionAck.class);
                Assert.assertEquals(ack.getContext().getIdObject(), "transactionId-1");

                accountManagerActor.tell(Account.GetBalance.accountId("10"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("10", 820d));
                accountManagerActor.tell(Account.GetBalance.accountId("20"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("20", 2180d));
            }
        };
    }

    @Test
    public void testMoneyTransferWithAccountInsufficientBalance() throws InterruptedException {
        new TestKit(actorSystem) {
            {
                final ActorRef probe = getRef();
                Map<String, Optional<Double>> balances = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(toMap(identity(), j -> Optional.of(Double.valueOf(j)*100)));
                final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountManagerActor.class, balances));
                final ActorRef moneyTransferActor = actorSystem.actorOf(Props.create(PaymentTransactionActor.class, accountManagerActor));

                accountManagerActor.tell(Account.GetBalance.accountId("10"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("10", 1000d));
                accountManagerActor.tell(Account.GetBalance.accountId("20"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("20", 2000d));

                Transaction originalTransaction = Transaction.init("10", "20", 1100, Context.init("transactionId-1", probe, 1));
                moneyTransferActor.tell(originalTransaction, probe);

                Account.FailedTransactionDetail failure = expectMsgClass(Account.FailedTransactionDetail.class);
                Assert.assertTrue(failure.getFailureReason().cause() instanceof Account.AccountInsufficientBalance);
                Assert.assertEquals(failure.transaction(), originalTransaction);
                Assert.assertEquals(failure.transaction().fromAccount(), originalTransaction.fromAccount());
                Assert.assertEquals(failure.transaction().toAccount(), originalTransaction.toAccount());
                Assert.assertEquals(failure.transaction().amount(), originalTransaction.amount(), 0.01);
            }
        };
    }

    @Test
    public void testMoneyTransferWithNegativeTransferAmount() throws InterruptedException {
        new TestKit(actorSystem) {
            {
                final ActorRef probe = getRef();
                Map<String, Optional<Double>> balances = IntStream.range(1, 1000).mapToObj(String::valueOf).collect(toMap(identity(), j -> Optional.of(Double.valueOf(j)*100)));
                final ActorRef accountManagerActor = actorSystem.actorOf(Props.create(AccountManagerActor.class, balances));
                final ActorRef moneyTransferActor = actorSystem.actorOf(Props.create(PaymentTransactionActor.class, accountManagerActor));

                accountManagerActor.tell(Account.GetBalance.accountId("10"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("10", 1000d));
                accountManagerActor.tell(Account.GetBalance.accountId("20"), probe);
                expectMsgEquals(Account.ViewBalanceResponse.init("20", 2000d));

                Transaction originalTransaction = Transaction.init("10", "20", -180, Context.init("transactionId-1", probe, 1));
                moneyTransferActor.tell(originalTransaction, probe);

                Account.FailedTransactionDetail failure = expectMsgClass(Account.FailedTransactionDetail.class);
                Assert.assertTrue(failure.getFailureReason().cause() instanceof Account.NegativeTransferAmount);
                Assert.assertEquals(failure.transaction(), originalTransaction);
                Assert.assertEquals(failure.transaction().fromAccount(), originalTransaction.fromAccount());
                Assert.assertEquals(failure.transaction().toAccount(), originalTransaction.toAccount());
                Assert.assertEquals(failure.transaction().amount(), originalTransaction.amount(), 0.01);
            }
        };
    }
}