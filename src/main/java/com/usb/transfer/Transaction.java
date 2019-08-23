package com.usb.transfer;

public class Transaction {
    private final String fromAccount;
    private final String toAccount;
    private final double amount;
    private final Context context;

    public Transaction(String fromAccount, String toAccount, double amount,  Context context) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.context = context;
    }

    public static Transaction init(String fromAccount, String toAccount, double amount,  Context context) {
        return new Transaction(fromAccount, toAccount, amount, context);
    }

    public String fromAccount() {
        return fromAccount;
    }

    public String toAccount() {
        return toAccount;
    }

    public double amount() {
        return amount;
    }

    public boolean shouldHavePositiveAmount() {
            return amount > 0;
    }

    public Context transactionContext() {
        return this.context;
    }

    @Override
    public String toString() {
        return "Transaction_" +
                "fromAccount-" + fromAccount +
                "-toAccount-" + toAccount +
                "-amount-" + amount;
    }
}

