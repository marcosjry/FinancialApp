package com.marcos.budget.service.domain.exception;

public class BudgetNameDoesntExistException extends Exception{
    public BudgetNameDoesntExistException(String message) {
        super(message);
    }
}
