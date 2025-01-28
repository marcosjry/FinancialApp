package com.marcos.budget.service.domain.service;



import com.marcos.budget.service.domain.DTO.budget.TransactionDTO;
import com.marcos.budget.service.domain.model.TransactionBudget;

import java.security.InvalidParameterException;

public interface TransactionService {

    TransactionBudget createTransaction(TransactionDTO transactionDTO, String userId, String budgetId) throws InvalidParameterException;
}
