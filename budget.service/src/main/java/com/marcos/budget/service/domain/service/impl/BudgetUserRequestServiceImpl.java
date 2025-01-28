package com.marcos.budget.service.domain.service.impl;

import com.google.gson.Gson;
import com.marcos.budget.service.domain.service.TransactionService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import com.marcos.budget.service.domain.DTO.request.RequestBudget;
import com.marcos.budget.service.domain.DTO.request.RequestCreateBudgetDTO;
import com.marcos.budget.service.domain.DTO.request.RequestCreateTransactionBudget;
import com.marcos.budget.service.domain.DTO.request.RequestDeleteBudget;
import com.marcos.budget.service.domain.DTO.response.ResponseToOrchestrator;
import com.marcos.budget.service.domain.config.infra.ConnectionPool;
import com.marcos.budget.service.domain.exception.BudgetAlreadyExistsException;
import com.marcos.budget.service.domain.exception.BudgetNameOrUserIdException;
import com.marcos.budget.service.domain.model.Budget;
import com.marcos.budget.service.domain.model.TransactionBudget;
import com.marcos.budget.service.domain.service.BudgetService;
import com.marcos.budget.service.domain.service.BudgetUserRequestService;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.NoSuchElementException;

@Component
public class BudgetUserRequestServiceImpl implements BudgetUserRequestService {

    @Autowired
    private ConnectionPool connectionPool;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Gson gson;

    @Autowired
    private TransactionService transactionService;

    private Channel channel;


    @PostConstruct
    @Override
    public void receiveBudgetRequest() {
        {
            try {
                // Obtendo um canal do pool
                channel = connectionPool.acquireChannel();

                // Configurando o consumidor
                channel.basicConsume("orchestrator_from_user_request", true, (consumerTag, delivery) -> {

                    String correlationId = delivery.getProperties().getCorrelationId(); // Obtendo o correlationId
                    String messageBody = new String(delivery.getBody(), StandardCharsets.UTF_8);


                    // Processando a mensagem
                    RequestBudget receivedRequest = gson.fromJson(messageBody, RequestBudget.class);
                    ResponseToOrchestrator response = new ResponseToOrchestrator();

                    switch (receivedRequest.getAction()) {
                        case "create_budget":
                            this.handleCreateBudget((RequestCreateBudgetDTO) receivedRequest.getData(), response);
                            break;
                        case "delete_budget":
                            this.handleDeleteBudget((RequestDeleteBudget) receivedRequest.getData(), response);
                            break;
                        case "create_budget_transaction":
                            this.handleCreateTransaction((RequestCreateTransactionBudget) receivedRequest.getData(), response);
                            break;
                        default:
                            response.setMessage("Action not supported: " + receivedRequest.getAction());
                    }

                    // Preparando a mensagem de resposta
                    AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                            .correlationId(correlationId) // Incluindo o correlationId na resposta
                            .build();

                    response.setAction(receivedRequest.getAction());
                    String responseToRequest = gson.toJson(response);

                    // Publicando na fila de resposta
                    channel.basicPublish("", "budget_response_to_user_request", props, responseToRequest.getBytes(StandardCharsets.UTF_8));

                    System.out.println("Message sent -> { " + responseToRequest + " }");

                }, consumerTag -> {
                    // Callback para cancelamento
                    System.out.println("Consumer canceled: " + consumerTag);
                });

                System.out.println("Listening for messages on 'orchestrator_from_user_request'...");

            } catch (Exception e) {
                System.err.println("Error while consuming messages: " + e.getMessage());
            }
        }
    }


    @PreDestroy
    public void stopConsuming() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                System.out.println("Channel closed and consumer stopped.");
            }
        } catch (Exception e) {
            System.err.println("Error while stopping consumer: " + e.getMessage());
        } finally {
            connectionPool.releaseChannel(channel);
        }
    }

    private void handleCreateBudget(RequestCreateBudgetDTO request, ResponseToOrchestrator response) {
        try {
            this.budgetService.createBudget(request.getBudgetDTO(), request.getUserId());
            response.setMessage("Budget created.");
        } catch (BudgetAlreadyExistsException e) {
            response.setMessage("Budget already exists.");
        } catch (Exception e) {
            response.setMessage("Error during budget creation.");
        }
    }

    private void handleDeleteBudget(RequestDeleteBudget requestDeleteBudget, ResponseToOrchestrator response) {
        try {
            this.budgetService.deleteBudgetByName(requestDeleteBudget.getBudgetName(), requestDeleteBudget.getUserId());
            response.setMessage("Budget deleted.");
        } catch (NoSuchElementException e) {
            response.setMessage("Budget name doesn't exist.");
        } catch (Exception e) {
            response.setMessage("Error during delete budget.");
        }
    }

    private void handleCreateTransaction(RequestCreateTransactionBudget createTransaction, ResponseToOrchestrator response) {
        try {
            String budgetName = createTransaction.getTransactionDTO().budgetName();
            Budget findedBudget = this.budgetService.findBudgetByNameAndUserId(budgetName, createTransaction.getUserId());
            if(findedBudget == null) {
                throw new BudgetNameOrUserIdException("Error with budgetName or userId.");
            }

            TransactionBudget createdTransaction = this.transactionService.createTransaction(createTransaction.getTransactionDTO(), createTransaction.getUserId(), findedBudget.getId());
            findedBudget.getBudgetTransaction().add(createdTransaction);
            findedBudget.calculaDespesaReceita();
            this.budgetService.saveBudget(findedBudget);
            response.setMessage("Transaction created.");

        } catch (InvalidParameterException e) {
            response.setMessage("Ivalid parameters, try again.");
        } catch (BudgetNameOrUserIdException e) {
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage("Error during create transaction, try again.");
        }
    }

}
