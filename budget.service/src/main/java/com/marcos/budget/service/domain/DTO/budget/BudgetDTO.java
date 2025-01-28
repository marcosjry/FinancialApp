package com.marcos.budget.service.domain.DTO.budget;

import java.time.LocalDateTime;

public record BudgetDTO(String name, Double value, String description, LocalDateTime dateStart, LocalDateTime dateEnd) {
}
