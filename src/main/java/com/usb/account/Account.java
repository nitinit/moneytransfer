package com.usb.account;

import akka.actor.Status;
import com.usb.transfer.Transaction;
import com.usb.transfer.Context;

import java.io.Serializable;

public class Account {

    public interface AccountCommand {
        public String getAccountId();
    };

    public static final class Deposit implements AccountCommand {
        final String accountId;
        final double amount;
        private Deposit(String accountId, double amount) {
            this.accountId = accountId;
            this.amount = amount;
        }

        public static Deposit init(String accountId, double amount) {
            return new Deposit(accountId, amount);
        }

        public String getAccountId() {
            return accountId;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static final class Withdraw implements AccountCommand {
        final String accountId;
        final double amount;
        private Withdraw(String accountId, double amount) {
            this.accountId = accountId;
            this.amount = amount;
        }

        public static Withdraw init(String accountId, double amount) {
            return new Withdraw(accountId, amount);
        }

        public String getAccountId() {
            return accountId;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static final class GetBalance implements AccountCommand {
        private final String accountId;
        private GetBalance(String accountId) {
            this.accountId = accountId;
        }

        public static GetBalance accountId(String accountId) {
            return new GetBalance(accountId);
        }

        public static GetBalance init(String accountId) {
            return new GetBalance(accountId);
        }

        public String getAccountId() {
            return accountId;
        }
    }

    public static final class ViewBalance {
        private Context context;

        private ViewBalance(Context context) {
            this.context = context;
        }

        public static ViewBalance init(Context context) {
            return new ViewBalance(context);
        }

        public Context context() {
            return context;
        }
    }

    public static class ViewBalanceResponse implements Serializable {
        private final String accountId;
        private final Double amount;
        private Context context;

        private ViewBalanceResponse(String accountId, Double amount, Context context) {
            this.accountId = accountId;
            this.amount = amount;
            this.context = context;
        }

        public static ViewBalanceResponse init(String accountId, Double amount) {
            return new ViewBalanceResponse(accountId, amount, null);
        }

        public static ViewBalanceResponse init(String accountId, Double amount, Context context) {
            return new ViewBalanceResponse(accountId, amount, context);
        }

        public Double amount() {
            return amount;
        }

        public String accountId() {
            return accountId;
        }

        public Context context() {
            return context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ViewBalanceResponse that = (ViewBalanceResponse) o;

            return amount.equals(that.amount);
        }

        @Override
        public int hashCode() {
            return amount.hashCode();
        }
    }

    public enum ViewAccountBalance {
        PRINT, DONE
    }

    public static class FailedTransactionDetail {
        private final Transaction transaction;
        private final Status.Failure failureReason;
        private FailedTransactionDetail(Transaction transaction, Status.Failure failureReason) {
            this.transaction = transaction;
            this.failureReason = failureReason;
        }

        public static FailedTransactionDetail init(Transaction transaction, Status.Failure failureReason) {
            return new FailedTransactionDetail(transaction, failureReason);
        }

        public Transaction transaction() {
            return this.transaction;
        }

        public Status.Failure getFailureReason() {
            return this.failureReason;
        }
    }

    public static class AccountNotFound extends RuntimeException{
        public AccountNotFound() {
            super();
        }
    }

    public static class AccountInsufficientBalance extends Exception{
        private static AccountInsufficientBalance accountInsufficientBalance = new AccountInsufficientBalance();
        public static AccountInsufficientBalance init() {
            return accountInsufficientBalance;
        }
    }

    public static class NegativeTransferAmount extends Exception{
        private static NegativeTransferAmount negativeTransferAmount = new NegativeTransferAmount();
        public static NegativeTransferAmount init() {
            return negativeTransferAmount;
        }
    }

    public static class TransactionAck {
        private final Context context;

        private TransactionAck(Context context) {
            this.context = context;
        }

        public static TransactionAck init(Context context) {
            return new TransactionAck(context);
        }

        public Context getContext() {
            return context;
        }
    }

    public enum TransactionStatus {DONE, FAILED}
}