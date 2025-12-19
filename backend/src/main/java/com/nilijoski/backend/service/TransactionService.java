package com.nilijoski.backend.service;

import com.nilijoski.backend.exception.AccountNotFoundException;
import com.nilijoski.backend.exception.InvalidIbanException;
import com.nilijoski.backend.exception.InvalidTransferAmountException;
import com.nilijoski.backend.exception.SameAccountTransferException;
import com.nilijoski.backend.model.Transaction;
import com.nilijoski.backend.model.User;
import com.nilijoski.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private static final String COMPLETED = "COMPLETED";
    
    @Transactional
    public Transaction transfer(String fromIban, String toIban, String toFirstName, String toLastName, BigDecimal amount, String description) {
        // Validate IBAN format
        if (!isValidIban(toIban)) {
            throw new InvalidIbanException("Invalid IBAN format");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferAmountException("Transfer amount must be positive");
        }

        if (fromIban.equals(toIban)) {
            throw new SameAccountTransferException("Cannot transfer money to your own account");
        }

        User fromUser;
        User toUser;

        try {
            fromUser = userService.getUserByIban(fromIban);
        } catch (Exception e) {
            throw new AccountNotFoundException("Your account not found");
        }

        try {
            toUser = userService.getUserByIban(toIban);
        } catch (Exception e) {
            throw new InvalidIbanException("Recipient IBAN not found. Please check the IBAN and try again.");
        }

        Transaction transaction = new Transaction();
        transaction.setFromIban(fromIban);
        transaction.setToIban(toIban);
        transaction.setFromFirstName(fromUser.getFirstName());
        transaction.setFromLastName(fromUser.getLastName());
        transaction.setToFirstName(toFirstName);
        transaction.setToLastName(toLastName);
        transaction.setFromAccountNumber(fromUser.getAccountNumber());
        transaction.setToAccountNumber(toUser.getAccountNumber());
        transaction.setAmount(amount);
        transaction.setTransactionType("TRANSFER");
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus("PENDING");

        if (!toUser.getFirstName().equalsIgnoreCase(toFirstName) ||
            !toUser.getLastName().equalsIgnoreCase(toLastName)) {
            transaction.setWarning("Name mismatch: Account holder is " + toUser.getFirstName() + " " + toUser.getLastName());
        }

        userService.withdraw(fromUser.getAccountNumber(), amount);

        userService.deposit(toUser.getAccountNumber(), amount);

        transaction.setStatus(COMPLETED);
        transactionRepository.save(transaction);

        return transaction;
    }

    public List<String> getRecipientIbanByUserIban(String userIban) {
        List<Transaction> sentTransactions = transactionRepository.findByFromIban(userIban);
        return sentTransactions.stream()
                .map(Transaction::getToIban)
                .distinct()
                .toList();
    }
    
    public Transaction createDepositTransaction(String accountNumber, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setToAccountNumber(accountNumber);
        transaction.setAmount(amount);
        transaction.setTransactionType("DEPOSIT");
        transaction.setStatus(COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
    
    public Transaction createWithdrawalTransaction(String accountNumber, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setFromAccountNumber(accountNumber);
        transaction.setAmount(amount);
        transaction.setTransactionType("WITHDRAWAL");
        transaction.setStatus(COMPLETED);
        transaction.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Transaction getTransactionById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }
    
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) {
        return transactionRepository.findByFromAccountNumberOrToAccountNumber(accountNumber, accountNumber);
    }

    public List<Transaction> getTransactionsByIban(String iban) {
        return transactionRepository.findByFromIbanOrToIban(iban, iban);
    }

    private boolean isValidIban(String iban) {
        if (iban == null || iban.isEmpty()) {
            return false;
        }

        String cleanIban = iban.replaceAll("\\s", "").toUpperCase();

        if (!cleanIban.matches("^[A-Z]{2}\\d{2}[A-Z\\d]{1,30}$")) {
            return false;
        }

        return !cleanIban.startsWith("DE") || cleanIban.length() == 22;
    }
}
