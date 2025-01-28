package com.marcos.budget.service.domain.exception;

public class BudgetAlreadyExistsException extends Exception {
    public BudgetAlreadyExistsException(String message) {
        super(message);
    }
}
