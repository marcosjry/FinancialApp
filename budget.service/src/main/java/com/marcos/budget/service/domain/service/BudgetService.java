package com.marcos.budget.service.domain.service;

import com.marcos.budget.service.domain.DTO.budget.BudgetDTO;
import com.marcos.budget.service.domain.exception.BudgetAlreadyExistsException;
import com.marcos.budget.service.domain.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BudgetService {

    Budget createBudget(BudgetDTO budgetDTO, String userId) throws BudgetAlreadyExistsException;

    Budget findById(String id);

    List<Budget> findBudgetByUserId(String userId);

    void deleteBudget(String BudgetId);

    void deleteBudgetByName(String budgetName, String userId);

    Budget findByName(String budgetName);

    Budget findBudgetByNameAndUserId(String budgetName, String userId);

    Page<Budget> findBudgets(String userId, Pageable pageable);

    void saveBudget(Budget budgetToSave);
}
