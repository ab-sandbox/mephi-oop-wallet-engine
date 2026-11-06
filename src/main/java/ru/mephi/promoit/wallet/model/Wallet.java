package ru.mephi.promoit.wallet.model;

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
        return transactions.stream()
            .map(transaction ->
                transaction.type() == TransactionType.INCOME
                    ? transaction.amount()
                    : transaction.amount().negate()
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}
