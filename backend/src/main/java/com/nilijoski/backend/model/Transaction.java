package com.nilijoski.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    private String fromIban;

    private String toIban;

    private String fromFirstName;

    private String fromLastName;

    private String toFirstName;

    private String toLastName;

    private String fromAccountNumber;

    private String toAccountNumber;

    private BigDecimal amount;

    private String transactionType;

    private String status;

    private String description;

    private String warning;

    private LocalDateTime transactionDate;
}
