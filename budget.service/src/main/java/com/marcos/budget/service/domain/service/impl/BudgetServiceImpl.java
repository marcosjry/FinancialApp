package com.marcos.budget.service.domain.service.impl;

import com.marcos.budget.service.domain.DTO.budget.BudgetDTO;
import com.marcos.budget.service.domain.exception.BudgetAlreadyExistsException;
import com.marcos.budget.service.domain.model.Budget;
import com.marcos.budget.service.domain.repository.BudgetRepository;
import com.marcos.budget.service.domain.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Override
    public Budget createBudget(BudgetDTO budgetDTO, String userId) throws BudgetAlreadyExistsException {

        String name = budgetDTO.name().toLowerCase().trim();
        userId = userId.trim(); // Normaliza o valueTransaction para evitar inconsistências

        // Buscar diretamente o orçamento pelo nome e ID do usuário
        Optional<Budget> existingBudget = budgetRepository.findSingleBudget(userId, name);

        if (existingBudget.isPresent()) {
            throw new BudgetAlreadyExistsException("A Budget with this name already exists.");
        }

        // Verificar se os dados do Budget são válidos
        if (!verifyBudgetDTOcredential(budgetDTO)) {
            throw new IllegalArgumentException("Invalid BudgetDTO credentials.");
        }

        // Criar e salvar um novo orçamento
        Budget newBudget = new Budget();
        newBudget.setUserId(userId);
        newBudget.setName(budgetDTO.name());
        newBudget.setValor(budgetDTO.value());
        newBudget.setDescription(budgetDTO.description());
        newBudget.setDateEnd(budgetDTO.dateEnd());
        newBudget.setDateStart(budgetDTO.dateStart());

        budgetRepository.save(newBudget);
        return newBudget;
    }

    @Override
    public Budget findById(String id){
        return this.budgetRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @Override
    public List<Budget> findBudgetByUserId(String userId) {
        return this.budgetRepository.findByUserId(userId);
    }

    @Override
    public void deleteBudget(String budgetId) {
        try {
            Budget findedBudget = this.findById(budgetId);
            if(findedBudget.getId().equals(budgetId)) {
                this.budgetRepository.deleteById(budgetId);
            }
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Budget ID not Found.");
        }
    }

    @Override
    public void deleteBudgetByName(String budgetName, String userId) {
        try {
            var budget = this.findBudgetByNameAndUserId(budgetName, userId);
            this.budgetRepository.delete(budget);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("Budget name not found.");
        }
    }

    @Override
    public Budget findByName(String budgetName) {
        try {
            return this.budgetRepository.findByName(budgetName);
        } catch (NullPointerException e) {
            return null;
        }

    }

    @Override
    public Budget findBudgetByNameAndUserId(String budgetName, String userId) {
        return this.budgetRepository.findByNameAndUserId(budgetName, userId);
    }

    @Override
    public Page<Budget> findBudgets(String userId, Pageable pageable) {
        return this.budgetRepository.findByUserId(userId, pageable);
    }

    @Override
    public void saveBudget(Budget budgetToSave) {
        this.budgetRepository.save(budgetToSave);
    }

    private boolean verifyBudgetDTOcredential(BudgetDTO budgetDTO) {
        // Verifica se todos os campos do DTO estão vazios ou nulos
        return (
                isNullOrEmpty(budgetDTO.name()) && isNullOrEmpty(budgetDTO.value().toString())
                && budgetDTO.value() > 0
        );
    }

    private boolean isNullOrEmpty(String value) {
        return value != null && !value.isEmpty();
    }

}
