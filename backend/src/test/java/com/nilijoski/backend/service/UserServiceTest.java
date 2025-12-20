package com.nilijoski.backend.service;

import com.nilijoski.backend.exception.AccountNotFoundException;
import com.nilijoski.backend.exception.InvalidTransferAmountException;
import com.nilijoski.backend.model.User;
import com.nilijoski.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("johndoe");
        testUser.setPassword(new BCryptPasswordEncoder().encode("password123"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setIban("DE89370400440532013000");
        testUser.setAccountNumber("1234567890");
        testUser.setBalance(BigDecimal.ZERO);
        testUser.setStatus("ACTIVE");
        testUser.setSavedRecipientIbans(new ArrayList<>());
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.register("johndoe", "password123", "John", "Doe");

        // Assert
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("DE89370400440532013000", result.getIban());
        verify(userRepository).existsByUsername("johndoe");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.register("johndoe", "password123", "John", "Doe"));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUsername("johndoe");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.login("johndoe", "password123");

        // Assert
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        verify(userRepository).findByUsername("johndoe");
    }

    @Test
    void login_InvalidUsername() {
        // Arrange
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.login("invalid", "password123"));

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername("invalid");
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.login("johndoe", "wrongpassword"));

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername("johndoe");
    }

    @Test
    void getUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("johndoe");

        // Assert
        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        verify(userRepository).findByUsername("johndoe");
    }

    @Test
    void getUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserByUsername("notfound"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> users = List.of(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserByAccountNumber_Success() {
        // Arrange
        when(userRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByAccountNumber("1234567890");

        // Assert
        assertNotNull(result);
        assertEquals("1234567890", result.getAccountNumber());
        verify(userRepository).findByAccountNumber("1234567890");
    }

    @Test
    void getUserByAccountNumber_NotFound() {
        // Arrange
        when(userRepository.findByAccountNumber("9999999999")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserByAccountNumber("9999999999"));

        assertTrue(exception.getMessage().contains("User not found with account number"));
    }

    @Test
    void addSavedRecipient_Success() {
        // Arrange
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.addSavedRecipient("user123", "DE89370400440532013001");

        // Assert
        assertNotNull(result);
        assertTrue(result.getSavedRecipientIbans().contains("DE89370400440532013001"));
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addSavedRecipient_AlreadyExists() {
        // Arrange
        testUser.getSavedRecipientIbans().add("DE89370400440532013001");
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.addSavedRecipient("user123", "DE89370400440532013001"));

        assertEquals("Recipient already saved", exception.getMessage());
        verify(userRepository).findById("user123");
        verify(userRepository, never()).save(any());
    }

    @Test
    void addSavedRecipient_UserNotFound() {
        // Arrange
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.addSavedRecipient("invalid", "DE89370400440532013001"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getSavedRecipients_Success() {
        // Arrange
        testUser.getSavedRecipientIbans().add("DE89370400440532013001");

        User recipient = new User();
        recipient.setId("user456");
        recipient.setIban("DE89370400440532013001");
        recipient.setFirstName("Jane");
        recipient.setLastName("Smith");

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.findByIban("DE89370400440532013001")).thenReturn(Optional.of(recipient));

        // Act
        List<User> result = userService.getSavedRecipients("user123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane", result.getFirst().getFirstName());
        verify(userRepository).findById("user123");
        verify(userRepository).findByIban("DE89370400440532013001");
    }

    @Test
    void getSavedRecipients_UserNotFound() {
        // Arrange
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getSavedRecipients("invalid"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void removeSavedRecipient_Success() {
        // Arrange
        testUser.getSavedRecipientIbans().add("DE89370400440532013001");
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.removeSavedRecipient("user123", "DE89370400440532013001");

        // Assert
        assertNotNull(result);
        assertFalse(result.getSavedRecipientIbans().contains("DE89370400440532013001"));
        verify(userRepository).findById("user123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void removeSavedRecipient_UserNotFound() {
        // Arrange
        when(userRepository.findById("invalid")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.removeSavedRecipient("invalid", "DE89370400440532013001"));

        assertEquals("User not found", exception.getMessage());
    }
    @Test
    void getAccountByAccountNumber_Success() {
        when(userRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(testUser));

        User result = userService.getAccountByAccountNumber("1234567890");

        assertNotNull(result);
        assertEquals("1234567890", result.getAccountNumber());
    }

    @Test
    void getAccountByAccountNumber_NotFound() {
        when(userRepository.findByAccountNumber("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> userService.getAccountByAccountNumber("invalid"));
    }

    @Test
    void getUserByIban_Success() {
        when(userRepository.findByIban(testUser.getIban()))
                .thenReturn(Optional.of(testUser));

        User result = userService.getUserByIban(testUser.getIban());

        assertEquals(testUser.getIban(), result.getIban());
    }

    @Test
    void getUserByIban_NotFound() {
        when(userRepository.findByIban("INVALID"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> userService.getUserByIban("INVALID"));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById("user123"))
                .thenReturn(Optional.of(testUser));

        User result = userService.getUserById("user123");

        assertEquals("user123", result.getId());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> userService.getUserById("invalid"));
    }

    @Test
    void deposit_Success() {
        testUser.setBalance(BigDecimal.valueOf(100));
        when(userRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(testUser));

        userService.deposit("1234567890", BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), testUser.getBalance());
        verify(userRepository).save(testUser);
    }

    @Test
    void withdraw_Success() {
        testUser.setBalance(BigDecimal.valueOf(100));
        when(userRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(testUser));

        userService.withdraw("1234567890", BigDecimal.valueOf(40));

        assertEquals(BigDecimal.valueOf(60), testUser.getBalance());
        verify(userRepository).save(testUser);
    }

    @Test
    void withdraw_InsufficientBalance() {
        testUser.setBalance(BigDecimal.valueOf(20));
        when(userRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(testUser));

        assertThrows(InvalidTransferAmountException.class,
                () -> userService.withdraw("1234567890", BigDecimal.valueOf(50)));
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById("user123"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        User update = new User();
        update.setFirstName("Updated");
        update.setBalance(BigDecimal.TEN);

        User result = userService.updateUser("user123", update);

        assertEquals("Updated", result.getFirstName());
        assertEquals(BigDecimal.TEN, result.getBalance());
    }

    @Test
    void deleteUser_Success() {
        userService.deleteUser("user123");

        verify(userRepository).deleteById("user123");
    }

}
