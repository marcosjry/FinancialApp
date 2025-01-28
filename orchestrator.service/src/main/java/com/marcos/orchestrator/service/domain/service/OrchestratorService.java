package com.marcos.orchestrator.service.domain.service;

import com.marcos.orchestrator.service.domain.DTO.auth.AuthUser;
import com.marcos.orchestrator.service.domain.DTO.user.budget.BudgetDTO;
import com.marcos.orchestrator.service.domain.DTO.user.budget.transaction.TransactionDTO;

public interface OrchestratorService {

    void receiveLoginAuthentication(String message);

    void sendMessagesToServices(AuthUser authUser);

    String sendAuthMessage(String queueName, String userId, String correlationId);

    String sendUserRequest(BudgetDTO budgetDTO, String userId, String action);

    String sendUserRequest(TransactionDTO transactionDTO, String userId, String action);
}
