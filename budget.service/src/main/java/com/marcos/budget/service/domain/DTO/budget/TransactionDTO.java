package com.marcos.budget.service.domain.DTO.budget;

import java.time.LocalDateTime;

public record TransactionDTO(String budgetName, String nameTransaction, String typeTransaction, int valueTransaction, LocalDateTime dateTransaction) {
}
