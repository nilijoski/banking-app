package com.nilijoski.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String iban;

    private String accountNumber;

    private BigDecimal balance;

    private String status;

    private List<String> savedRecipientIbans = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
