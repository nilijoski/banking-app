package com.nilijoski.backend.service;

import com.nilijoski.backend.exception.*;
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
import java.util.List;
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

    @Test
    void getRecipientIbanByUserIban_success() {
        Transaction t1 = new Transaction();
        t1.setToIban("IBAN1");

        Transaction t2 = new Transaction();
        t2.setToIban("IBAN2");

        Transaction t3 = new Transaction();
        t3.setToIban("IBAN1"); // duplicate

        when(transactionRepository.findByFromIban(fromUser.getIban()))
                .thenReturn(List.of(t1, t2, t3));

        List<String> result = transactionService.getRecipientIbanByUserIban(fromUser.getIban());

        assertEquals(2, result.size());
        assertTrue(result.contains("IBAN1"));
        assertTrue(result.contains("IBAN2"));
    }

    @Test
    void getAllTransactions_success() {
        when(transactionRepository.findAll()).thenReturn(List.of(new Transaction()));

        List<Transaction> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
        verify(transactionRepository).findAll();
    }

    @Test
    void getTransactionById_notFound_throwsTransactionNotFoundException() {
        when(transactionRepository.findById("tx1")).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById("tx1"));
    }

    @Test
    void getTransactionsByAccountNumber_success() {
        when(transactionRepository
                .findByFromAccountNumberOrToAccountNumber("12345", "12345"))
                .thenReturn(List.of(new Transaction()));

        List<Transaction> result =
                transactionService.getTransactionsByAccountNumber("12345");

        assertEquals(1, result.size());
    }

    @Test
    void getTransactionsByIban_success() {
        when(transactionRepository
                .findByFromIbanOrToIban(fromUser.getIban(), fromUser.getIban()))
                .thenReturn(List.of(new Transaction()));

        List<Transaction> result =
                transactionService.getTransactionsByIban(fromUser.getIban());

        assertEquals(1, result.size());
    }

    @Test
    void transfer_toAccountNotFound_throwsInvalidIbanException() {
        when(userService.getUserByIban(fromUser.getIban())).thenReturn(fromUser);
        when(userService.getUserByIban(toUser.getIban()))
                .thenThrow(new RuntimeException());

        InvalidIbanException ex = assertThrows(InvalidIbanException.class, () ->
                transactionService.transfer(
                        fromUser.getIban(),
                        toUser.getIban(),
                        toUser.getFirstName(),
                        toUser.getLastName(),
                        BigDecimal.TEN,
                        "Test"
                )
        );

        assertTrue(ex.getMessage().contains("Recipient IBAN not found"));
    }

}
