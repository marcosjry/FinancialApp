package com.marcos.budget.service.domain.model;

public enum TransactionType {
    RECEITA("receita"),
    DESPESA("despesa");

    TransactionType(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    private String tipo;


}
