package com.nilijoski.backend.service;

import com.nilijoski.backend.exception.AccountNotFoundException;
import com.nilijoski.backend.exception.InvalidIbanException;
import com.nilijoski.backend.exception.InvalidTransferAmountException;
import com.nilijoski.backend.exception.SameAccountTransferException;
import com.nilijoski.backend.model.Transaction;
import com.nilijoski.backend.model.User;
import com.nilijoski.backend.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionService transactionService;

    private User fromUser;
    private User toUser;

    @BeforeEach
    void setUp() {

        fromUser = new User();
        fromUser.setIban("DE89370400440532013000");
        fromUser.setFirstName("John");
        fromUser.setLastName("Doe");
        fromUser.setAccountNumber("12345");

        toUser = new User();
        toUser.setIban("DE75512108001245126199");
        toUser.setFirstName("Jane");
        toUser.setLastName("Smith");
        toUser.setAccountNumber("67890");
    }

    @Test
    void transfer_successful() {
        when(userService.getUserByIban(fromUser.getIban())).thenReturn(fromUser);
        when(userService.getUserByIban(toUser.getIban())).thenReturn(toUser);

        Transaction transaction = transactionService.transfer(
                fromUser.getIban(),
                toUser.getIban(),
                toUser.getFirstName(),
                toUser.getLastName(),
                new BigDecimal("100.00"),
                "Test transfer"
        );

        assertEquals("COMPLETED", transaction.getStatus());
        assertNull(transaction.getWarning());
        assertEquals("TRANSFER", transaction.getTransactionType());
        verify(userService).withdraw(fromUser.getAccountNumber(), new BigDecimal("100.00"));
        verify(userService).deposit(toUser.getAccountNumber(), new BigDecimal("100.00"));
        verify(transactionRepository).save(transaction);
    }

    @Test
    void transfer_invalidIban_throwsException() {
        InvalidIbanException exception = assertThrows(InvalidIbanException.class,
                () -> transactionService.transfer(
                        fromUser.getIban(),
                        "INVALID_IBAN",
                        toUser.getFirstName(),
                        toUser.getLastName(),
                        new BigDecimal("100.00"),
                        "Test"
                ));

        assertEquals("Invalid IBAN format", exception.getMessage());
    }

    @Test
    void transfer_negativeAmount_throwsException() {
        assertThrows(InvalidTransferAmountException.class, () ->
                transactionService.transfer(
                        fromUser.getIban(),
                        toUser.getIban(),
                        toUser.getFirstName(),
                        toUser.getLastName(),
                        new BigDecimal("-50"),
                        "Test"
                ));
    }

    @Test
    void transfer_sameAccount_throwsException() {
        assertThrows(SameAccountTransferException.class, () ->
                transactionService.transfer(
                        fromUser.getIban(),
                        fromUser.getIban(),
                        fromUser.getFirstName(),
                        fromUser.getLastName(),
                        new BigDecimal("50"),
                        "Test"
                ));
    }

    @Test
    void transfer_fromAccountNotFound_throwsException() {
        when(userService.getUserByIban(fromUser.getIban())).thenThrow(new RuntimeException());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                transactionService.transfer(
                        fromUser.getIban(),
                        toUser.getIban(),
                        toUser.getFirstName(),
                        toUser.getLastName(),
                        new BigDecimal("100"),
                        "Test"
                ));

        assertEquals("Your account not found", exception.getMessage());
    }

    @Test
    void transfer_toAccountNameMismatch_setsWarning() {
        when(userService.getUserByIban(fromUser.getIban())).thenReturn(fromUser);
        when(userService.getUserByIban(toUser.getIban())).thenReturn(toUser);

        Transaction transaction = transactionService.transfer(
                fromUser.getIban(),
                toUser.getIban(),
                "WrongFirst",
                "WrongLast",
                new BigDecimal("100.00"),
                "Test transfer"
        );

        assertNotNull(transaction.getWarning());
        assertTrue(transaction.getWarning().contains("Name mismatch"));
    }

    @Test
    void createDepositTransaction_successful() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction transaction = transactionService.createDepositTransaction("12345", new BigDecimal("200"));

        assertEquals("COMPLETED", transaction.getStatus());
        assertEquals("DEPOSIT", transaction.getTransactionType());
        assertEquals(new BigDecimal("200"), transaction.getAmount());
    }

    @Test
    void createWithdrawalTransaction_successful() {
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction transaction = transactionService.createWithdrawalTransaction("12345", new BigDecimal("150"));

        assertEquals("COMPLETED", transaction.getStatus());
        assertEquals("WITHDRAWAL", transaction.getTransactionType());
        assertEquals(new BigDecimal("150"), transaction.getAmount());
    }

    @Test
    void getTransactionById_found() {
        Transaction tx = new Transaction();
        tx.setFromAccountNumber("12345");
        when(transactionRepository.findById("tx1")).thenReturn(Optional.of(tx));

        Transaction result = transactionService.getTransactionById("tx1");

        assertEquals("12345", result.getFromAccountNumber());
    }

    @Test
    void getTransactionById_notFound_throwsException() {
        when(transactionRepository.findById("tx1")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.getTransactionById("tx1"));

        assertTrue(exception.getMessage().contains("Transaction not found with id"));
    }
}
