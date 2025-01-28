package com.marcos.budget.service.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "BUDGET_TB")
@Entity
public class Budget {

    private String userId;

    public Budget() { this.budgetTransaction = new ArrayList<>(); }

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String id;
    private String name;
    private Double valor;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private Double income;
    private Double bills;
    private String description;

    @OneToMany(mappedBy = "budgetId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TransactionBudget> budgetTransaction;

    public Double getBills() {
        return bills;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBills(Double bills) {
        this.bills = bills;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }


    public LocalDateTime getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDateTime dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDateTime getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDateTime dateEnd) {
        this.dateEnd = dateEnd;
    }

    public List<TransactionBudget> getBudgetTransaction() {
        return budgetTransaction;
    }

    public void setBudgetTransaction(List<TransactionBudget> budgetTransaction) {
        this.budgetTransaction = budgetTransaction;
    }

    public Double getValor() {
        return valor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void calculaDespesaReceita() {
        this.bills = 0.0;
        this.income = 0.0;
        this.budgetTransaction.forEach(this::atualizaValor);
    }

    private void atualizaValor(TransactionBudget transacao) {
        switch (transacao.getTransactionType()) {
            case RECEITA:
                this.income += transacao.getTransactionAmount();
                break;
            case DESPESA:
                this.bills += transacao.getTransactionAmount();
                break;
            default:
                throw new IllegalArgumentException("Tipo de transação inválido: " + transacao.getTransactionType().getTipo());
        }
    }
}
