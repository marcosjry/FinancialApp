package com.marcos.budget.service.domain.DTO.request;


import com.marcos.budget.service.domain.DTO.budget.TransactionDTO;

import java.io.Serializable;

public class RequestCreateTransactionBudget implements Serializable {

    private String userId;
    private TransactionDTO transactionDTO;

    public TransactionDTO getTransactionDTO() {
        return transactionDTO;
    }

    public void setTransactionDTO(TransactionDTO transactionDTO) {
        this.transactionDTO = transactionDTO;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
