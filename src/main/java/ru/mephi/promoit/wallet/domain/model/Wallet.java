package ru.mephi.promoit.wallet.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Wallet {

    private final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction must not be null");
        }

        transactions.add(transaction);
    }

    public BigDecimal getBalance() {
        BigDecimal balance = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.type() == TransactionType.INCOME) {
                balance = balance.add(transaction.amount());
            } else {
                balance = balance.subtract(transaction.amount());
            }
        }

        return balance;
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}
