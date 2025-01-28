package com.marcos.budget.service.domain.service.impl;

import com.marcos.budget.service.domain.DTO.budget.TransactionDTO;
import com.marcos.budget.service.domain.model.TransactionBudget;
import com.marcos.budget.service.domain.model.TransactionType;
import com.marcos.budget.service.domain.repository.TransactionRepository;
import com.marcos.budget.service.domain.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;


@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;


    @Override
    public TransactionBudget createTransaction(TransactionDTO transactionDTO, String userId, String budgetId) {
        try {
            boolean isValid = this.verifyBudgetDTOcredential(transactionDTO);
            if(!isValid) throw new InvalidParameterException("Invalid parameters");

            TransactionBudget transaction = new TransactionBudget();
            transaction.setBudgetId(budgetId);
            transaction.setDate(transactionDTO.dateTransaction());
            transaction.setTransactionType(TransactionType.valueOf(transactionDTO.typeTransaction().toUpperCase()));
            transaction.setName(transactionDTO.nameTransaction());
            transaction.setTransactionAmount(transactionDTO.valueTransaction());
            transaction.setLocalDate(LocalDateTime.now());
            return this.transactionRepository.save(transaction);
        } catch (InvalidParameterException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    private boolean verifyBudgetDTOcredential(TransactionDTO transactionDTO) {
        // Verifica se todos os campos do DTO estão vazios ou nulos
        return (
                isNullOrEmpty(transactionDTO.budgetName()) &&
                        isNullOrEmpty(transactionDTO.typeTransaction()) &&
                        isNullOrEmpty(transactionDTO.nameTransaction()) &&
                        isNullOrEmpty(transactionDTO.dateTransaction().toString()) &&
                        transactionDTO.valueTransaction() > 0
        );
    }

    private boolean isNullOrEmpty(String value) {
        return value != null && !value.isEmpty();
    }

}
