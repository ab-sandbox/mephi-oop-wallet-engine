package ru.mephi.promoit.wallet.service;

import ru.mephi.promoit.wallet.model.Transaction;
import ru.mephi.promoit.wallet.model.TransactionType;
import ru.mephi.promoit.wallet.model.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class FinanceService {

    private final Wallet wallet;

    public FinanceService(Wallet wallet) {
        this.wallet = wallet;
    }

    public void addIncome(String category, BigDecimal amount) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category must not be blank");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Transaction transaction = new Transaction(
            UUID.randomUUID(),
            TransactionType.INCOME,
            UUID.randomUUID(),
            amount,
            Instant.now(),
            null
        );

        wallet.addTransaction(transaction);
    }

    public BigDecimal getBalance() {
        return wallet.getBalance();
    }
}
