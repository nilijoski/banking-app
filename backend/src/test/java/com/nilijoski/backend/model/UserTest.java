package com.nilijoski.backend.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void noArgsConstructor_CreatesEmptyUser() {
        User user = new User();
        // Assert
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getIban());
        assertNull(user.getAccountNumber());
        assertNotNull(user.getSavedRecipientIbans());
        assertTrue(user.getSavedRecipientIbans().isEmpty());
        assertNull(user.getCreatedAt());
    }

    @Test
    void allArgsConstructor_CreatesUserWithAllFields() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<String> recipients = new ArrayList<>(List.of("DE89370400440532013001"));

        // Act
        User user = new User(
                "user123",
                "johndoe",
                "hashedPassword",
                "John",
                "Doe",
                "DE89370400440532013000",
                "1234567890",
                BigDecimal.ZERO,
                "ACTIVE",
                recipients,
                now,
                now
        );

        // Assert
        assertEquals("user123", user.getId());
        assertEquals("johndoe", user.getUsername());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("DE89370400440532013000", user.getIban());
        assertEquals("1234567890", user.getAccountNumber());
        assertEquals(1, user.getSavedRecipientIbans().size());
        assertEquals("DE89370400440532013001", user.getSavedRecipientIbans().get(0));
        assertEquals(now, user.getCreatedAt());
    }


    @Test
    void savedRecipientIbans_CanBeEmpty() {
        // Arrange
        User user = new User();
        List<String> emptyList = new ArrayList<>();

        // Act
        user.setSavedRecipientIbans(emptyList);

        // Assert
        assertNotNull(user.getSavedRecipientIbans());
        assertEquals(0, user.getSavedRecipientIbans().size());
        assertTrue(user.getSavedRecipientIbans().isEmpty());
    }

    @Test
    void savedRecipientIbans_CanContainMultipleRecipients() {
        // Arrange
        User user = new User();
        List<String> recipients = new ArrayList<>();
        recipients.add("DE89370400440532013001");
        recipients.add("DE89370400440532013002");
        recipients.add("DE89370400440532013003");

        // Act
        user.setSavedRecipientIbans(recipients);

        // Assert
        assertEquals(3, user.getSavedRecipientIbans().size());
        assertTrue(user.getSavedRecipientIbans().contains("DE89370400440532013001"));
        assertTrue(user.getSavedRecipientIbans().contains("DE89370400440532013002"));
        assertTrue(user.getSavedRecipientIbans().contains("DE89370400440532013003"));
    }

    @Test
    void savedRecipientIbans_CanBeModified() {
        // Arrange
        User user = new User();
        List<String> recipients = new ArrayList<>();
        recipients.add("DE89370400440532013001");
        user.setSavedRecipientIbans(recipients);

        // Act
        user.getSavedRecipientIbans().add("DE89370400440532013002");

        // Assert
        assertEquals(2, user.getSavedRecipientIbans().size());
    }

    @Test
    void username_IsUnique() {
        // Arrange
        User user1 = new User();
        User user2 = new User();

        // Act
        user1.setUsername("johndoe");
        user2.setUsername("janedoe");

        // Assert
        assertNotEquals(user1.getUsername(), user2.getUsername());
    }

    @Test
    void password_StoresHashedValue() {
        // Arrange
        User user = new User();
        String hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMye";

        // Act
        user.setPassword(hashedPassword);

        // Assert
        assertEquals(hashedPassword, user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));
    }

    @Test
    void iban_LinksToAccount() {
        // Arrange
        User user = new User();
        String iban = "DE89370400440532013000";

        // Act
        user.setIban(iban);

        // Assert
        assertEquals(iban, user.getIban());
        assertEquals(22, user.getIban().length());
    }

    @Test
    void accountNumber_LinksToAccount() {
        // Arrange
        User user = new User();
        String accountNumber = "1234567890";

        // Act
        user.setAccountNumber(accountNumber);

        // Assert
        assertEquals(accountNumber, user.getAccountNumber());
        assertEquals(10, user.getAccountNumber().length());
    }

    @Test
    void createdAt_RecordsCreationTime() {
        // Arrange
        User user = new User();
        LocalDateTime created = LocalDateTime.now();

        // Act
        user.setCreatedAt(created);

        // Assert
        assertEquals(created, user.getCreatedAt());
        assertNotNull(user.getCreatedAt());
    }
}
