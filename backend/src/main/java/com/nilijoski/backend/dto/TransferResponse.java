package com.nilijoski.backend.dto;

import com.nilijoski.backend.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private boolean success;
    private String message;
    private Transaction transaction;

    public static TransferResponse success(Transaction transaction) {
        TransferResponse response = new TransferResponse();
        response.setSuccess(true);
        response.setTransaction(transaction);
        return response;
    }

    public static TransferResponse error(String message) {
        TransferResponse response = new TransferResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
