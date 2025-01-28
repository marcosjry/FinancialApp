package com.marcos.budget.service.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class TransactionBudget {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String id;

    private String name;
    private TransactionType transactionType;

    private int transactionAmount;

    private LocalDateTime date;

    private LocalDateTime localDate;

    private String budgetId; // Relacionamento com o orçamento

    public String getBudgetId() {
        return budgetId;
    }

    public LocalDateTime getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDateTime localDate) {
        this.localDate = localDate;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setBudgetId(String budgetId) {
        this.budgetId = budgetId;
    }

    public int getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(int transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setId(String id) {
        this.id = id;
    }
}
