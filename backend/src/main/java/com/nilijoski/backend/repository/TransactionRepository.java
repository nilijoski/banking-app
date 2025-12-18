package com.nilijoski.backend.repository;

import com.nilijoski.backend.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByFromAccountNumberOrToAccountNumber(String fromAccountNumber, String toAccountNumber);

    List<Transaction> findByFromIban(String fromIban);

    List<Transaction> findByFromIbanOrToIban(String fromIban, String toIban);
}
