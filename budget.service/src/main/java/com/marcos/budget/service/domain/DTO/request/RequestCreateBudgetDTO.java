package com.marcos.budget.service.domain.DTO.request;



import com.marcos.budget.service.domain.DTO.budget.BudgetDTO;

import java.io.Serializable;

public class RequestCreateBudgetDTO implements Serializable {

    private BudgetDTO budgetDTO;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BudgetDTO getBudgetDTO() {
        return budgetDTO;
    }

    public void setBudgetDTO(BudgetDTO budgetDTO) {
        this.budgetDTO = budgetDTO;
    }
}
