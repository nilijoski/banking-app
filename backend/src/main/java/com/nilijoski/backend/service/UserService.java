package com.nilijoski.backend.service;

import com.nilijoski.backend.exception.*;
import com.nilijoski.backend.model.User;
import com.nilijoski.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Random random = new Random();
    private static final String USR_NOT_FOUND = "User not found";

    public User register(String username, String password, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameExistsException("Username already exists");
        }

        // Create user with account details
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAccountNumber(generateAccountNumber());
        user.setIban(generateIban());
        user.setBalance(new BigDecimal("1000.00"));
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private String generateAccountNumber() {
        return String.format("%010d", random.nextInt(1000000000));
    }

    private String generateIban() {
        return "DE" + String.format("%020d", random.nextLong() & Long.MAX_VALUE).substring(0, 20);
    }
    
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidLoginException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidLoginException("Invalid username or password");
        }

        return user;
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException(USR_NOT_FOUND));
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("User not found with account number: " + accountNumber));
    }

    public User addSavedRecipient(String userId, String recipientIban) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException(USR_NOT_FOUND));

        if (user.getSavedRecipientIbans().contains(recipientIban)) {
            throw new RecipientSavedException("Recipient already saved");
        }

        user.getSavedRecipientIbans().add(recipientIban);
        return userRepository.save(user);
    }

    public List<User> getSavedRecipients(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException(USR_NOT_FOUND));

        return user.getSavedRecipientIbans().stream()
                .map(iban -> userRepository.findByIban(iban)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    public User removeSavedRecipient(String userId, String recipientIban) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException(USR_NOT_FOUND));

        user.getSavedRecipientIbans().remove(recipientIban);
        return userRepository.save(user);
    }

    public User getAccountByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with account number: " + accountNumber));
    }

    public User getUserByIban(String iban) {
        return userRepository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException("User not found with IBAN: " + iban));
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("User not found with id: " + id));
    }

    public void deposit(String accountNumber, BigDecimal amount) {
        User user = getUserByAccountNumber(accountNumber);
        user.setBalance(user.getBalance().add(amount));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void withdraw(String accountNumber, BigDecimal amount) {
        User user = getUserByAccountNumber(accountNumber);
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InvalidTransferAmountException("Insufficient balance");
        }
        user.setBalance(user.getBalance().subtract(amount));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);

        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }
        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }
        if (userDetails.getBalance() != null) {
            user.setBalance(userDetails.getBalance());
        }
        if (userDetails.getStatus() != null) {
            user.setStatus(userDetails.getStatus());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
