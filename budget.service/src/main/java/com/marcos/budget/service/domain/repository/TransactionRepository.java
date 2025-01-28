package com.marcos.budget.service.domain.repository;

import com.marcos.budget.service.domain.model.TransactionBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository <TransactionBudget, String> {
}
