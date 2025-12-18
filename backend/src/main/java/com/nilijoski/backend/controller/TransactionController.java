package com.nilijoski.backend.controller;

import com.nilijoski.backend.dto.TransferResponse;
import com.nilijoski.backend.model.Transaction;
import com.nilijoski.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(@RequestBody Map<String, Object> request) {
        try {
            String fromIban = (String) request.get("fromIban");
            String toIban = (String) request.get("toIban");
            String toFirstName = (String) request.get("toFirstName");
            String toLastName = (String) request.get("toLastName");
            double amountDouble = ((Number) request.get("amount")).doubleValue();
            String description = (String) request.get("description");

            Transaction transaction = transactionService.transfer(
                    fromIban,
                    toIban,
                    toFirstName,
                    toLastName,
                    java.math.BigDecimal.valueOf(amountDouble),
                    description
            );
            return new ResponseEntity<>(TransferResponse.success(transaction), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(TransferResponse.error(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(TransferResponse.error("An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recipients/{iban}")
    public ResponseEntity<List<String>> getRecipientsByUserIban(@PathVariable String iban) {
        List<String> recipientIbans = transactionService.getRecipientIbanByUserIban(iban);
        return ResponseEntity.ok(recipientIbans);
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<List<Transaction>> getTransactionsByIban(@PathVariable String iban) {
        List<Transaction> transactions = transactionService.getTransactionsByIban(iban);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }
    
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactions);
    }
}
