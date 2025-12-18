package com.nilijoski.backend.controller;

import com.nilijoski.backend.model.Transaction;
import com.nilijoski.backend.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void testTransferSuccess() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setId("123");
        when(transactionService.transfer(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(transaction);

        String requestBody = """
                {
                    "fromIban": "DE123",
                    "toIban": "DE456",
                    "toFirstName": "John",
                    "toLastName": "Doe",
                    "amount": 100,
                    "description": "Test transfer"
                }
                """;

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transaction.id").value("123"));
    }

    @Test
    void testTransferError() throws Exception {
        when(transactionService.transfer(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class), anyString()))
                .thenThrow(new RuntimeException("Invalid IBAN"));

        String requestBody = """
                {
                    "fromIban": "DE123",
                    "toIban": "INVALID",
                    "toFirstName": "John",
                    "toLastName": "Doe",
                    "amount": 100,
                    "description": "Test transfer"
                }
                """;

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid IBAN"));
    }

    @Test
    void testGetRecipientsByUserIban() throws Exception {
        List<String> recipients = Arrays.asList("DE111", "DE222");
        when(transactionService.getRecipientIbanByUserIban("DE123")).thenReturn(recipients);

        mockMvc.perform(get("/api/transactions/recipients/DE123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("DE111"))
                .andExpect(jsonPath("$[1]").value("DE222"));
    }

    @Test
    void testGetTransactionsByIban() throws Exception {
        Transaction t1 = new Transaction();
        t1.setId("1");
        Transaction t2 = new Transaction();
        t2.setId("2");
        when(transactionService.getTransactionsByIban("DE123")).thenReturn(Arrays.asList(t1, t2));

        mockMvc.perform(get("/api/transactions/iban/DE123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    void testGetAllTransactions() throws Exception {
        Transaction t1 = new Transaction();
        t1.setId("1");
        when(transactionService.getAllTransactions()).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void testGetTransactionById() throws Exception {
        Transaction t = new Transaction();
        t.setId("123");
        when(transactionService.getTransactionById("123")).thenReturn(t);

        mockMvc.perform(get("/api/transactions/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    void testGetTransactionsByAccountNumber() throws Exception {
        Transaction t1 = new Transaction();
        t1.setId("1");
        when(transactionService.getTransactionsByAccountNumber("ACC123")).thenReturn(List.of(t1));

        mockMvc.perform(get("/api/transactions/account/ACC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }
}
