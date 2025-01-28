package com.marcos.orchestrator.service.domain.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marcos.orchestrator.service.domain.DTO.auth.AuthUser;
import com.marcos.orchestrator.service.domain.DTO.user.RequestToBudgetQueue;
import com.marcos.orchestrator.service.domain.DTO.user.budget.BudgetDTO;
import com.marcos.orchestrator.service.domain.DTO.user.budget.BudgetResponse;
import com.marcos.orchestrator.service.domain.DTO.user.budget.RequestCreateBudgetMessage;
import com.marcos.orchestrator.service.domain.DTO.user.budget.transaction.RequestCreateTransactionBudget;
import com.marcos.orchestrator.service.domain.DTO.user.budget.transaction.TransactionDTO;
import com.marcos.orchestrator.service.domain.config.infra.ConnectionPool;
import com.marcos.orchestrator.service.domain.config.infra.LocalDateTimeAdapter;
import com.marcos.orchestrator.service.domain.service.OrchestratorService;
import com.marcos.orchestrator.service.domain.service.RabbitMQService;
import com.marcos.orchestrator.service.domain.service.TokenService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class OrchestratorServiceImpl implements OrchestratorService {

    @Autowired
    private ConnectionPool connectionPool;

    @Autowired
    private RabbitMQService rabbitMQService;

    @Autowired
    private TokenService tokenService;


    @RabbitListener(queues = "authenticated_user")
    @Override
    public void receiveLoginAuthentication(String message) {
        Gson gson = new Gson();

        AuthUser authenticatedUser = gson.fromJson(message, AuthUser.class);

        sendMessagesToServices(authenticatedUser);
    }

    @Override
    public void sendMessagesToServices(AuthUser authUser) {

        String budgetMessage = sendAuthMessage("orchestrator_to_budget", authUser.userId(),  authUser.correlationId());

        System.out.println(budgetMessage);
    }

    @Override
    public String sendAuthMessage(String queueName, String userId, String correlationId) {

        Channel channel = this.connectionPool.acquireChannel();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .build();

        // Reaproveitando instância de Gson
        Gson gson = createGsonInstance();

        try {
            // Enviando mensagem
            channel.basicPublish("", queueName, props, userId.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent message to queue: " + " with correlationId: " + correlationId);

            return processResponseFromAuth(gson, channel, correlationId, "budget_to_orchestrator");

        } catch (IOException e) {
            System.out.println("Error while sending message to Queue");
            return e.getMessage();
        }
    }

    private String processResponseFromAuth(Gson gson, Channel channel, String correlationId, String queueNameToReceive) throws IOException {
        CompletableFuture<String> future = new CompletableFuture<>();

        String consumerTag = channel.basicConsume(
                queueNameToReceive,
                true,
                handleDeliverCallBackFromAuth(future, gson, correlationId),
                consumer -> {}
        );

        try {
            // Espera a resposta do callback
            return future.get(30, TimeUnit.SECONDS); // Timeout de 30 segundos
        } catch (TimeoutException e) {
            return "Timeout while waiting for response";
        } catch (Exception e) {
            return "Error while processing response: " + e.getMessage();
        } finally {
            try {
                channel.basicCancel(consumerTag); // Cancela o consumidor antes de liberar o canal
            } catch (IOException e) {
                System.err.println("Erro ao cancelar o consumidor: " + e.getMessage());
            }
            connectionPool.releaseChannel(channel); // Libera o canal de volta ao pool
        }
    }

    public DeliverCallback handleDeliverCallBackFromAuth(CompletableFuture<String> future, Gson gson, String correlationId) {
        return (consumerTag, delivery) -> {
            try {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                String receivedCorrelationId = delivery.getProperties().getCorrelationId();

                if (!correlationId.equals(receivedCorrelationId)) {
                    System.err.println("CorrelationId mismatch. Ignoring message.");
                    future.complete("Wrong correlation ID.");
                    return;
                }

                System.out.println("Reponse Received -> { " + message + " }");

                future.complete(message);

            } catch (Exception e) {
                System.err.println("Error receiving message from rabbitmq " + e);
                future.completeExceptionally(e);
            }
        };
    }

    @Override
    public String sendUserRequest(BudgetDTO budgetDTO, String userId, String action) {

        BudgetDTO budget = new BudgetDTO(budgetDTO.name().toLowerCase(Locale.ROOT),
                budgetDTO.description(),
                budgetDTO.value(),
                budgetDTO.dateStart(),
                budgetDTO.dateEnd()
        );

        Channel channel = this.connectionPool.acquireChannel();

        // Reaproveitando instância de Gson
        Gson gson = createGsonInstance();

        // Criando CorrelationId
        String correlationId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .build();

        // Preparando a mensagem
        RequestCreateBudgetMessage requestData = new RequestCreateBudgetMessage( userId, budget );
        RequestToBudgetQueue request = new RequestToBudgetQueue( action, requestData );

        String requestToBudget = gson.toJson(request);

        try {
            // Enviando mensagem
            channel.basicPublish("", "orchestrator_from_user_request", props, requestToBudget.getBytes(StandardCharsets.UTF_8));

            return processResponse(gson, channel, correlationId, "budget_response_to_user_request");
        } catch (IOException e) {
            return "Error on publish message " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public String sendUserRequest(TransactionDTO transactionDTO, String userId, String action) {

        TransactionDTO transaction = new TransactionDTO(
                transactionDTO.budgetName().toLowerCase(Locale.ROOT),
                transactionDTO.nameTransaction().toLowerCase(Locale.ROOT),
                transactionDTO.typeTransaction().toLowerCase(Locale.ROOT),
                transactionDTO.valueTransaction(),
                transactionDTO.dateTransaction()
        );

        Channel channel = this.connectionPool.acquireChannel();

        // Reaproveitando instância de Gson
        Gson gson = createGsonInstance();

        // Criando CorrelationId
        String correlationId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .build();

        // Preparando a mensagem
        RequestCreateTransactionBudget requestData = new RequestCreateTransactionBudget( userId, transaction );
        RequestToBudgetQueue request = new RequestToBudgetQueue( action, requestData );

        String requestToBudget = gson.toJson(request);

        try {
            // Enviando mensagem
            channel.basicPublish("", "orchestrator_from_user_request", props, requestToBudget.getBytes(StandardCharsets.UTF_8));

            return processResponse(gson, channel, correlationId, "budget_response_to_user_request");
        } catch (IOException e) {
            return "Error on publish message " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private Gson createGsonInstance() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    private String processResponse(Gson gson, Channel channel, String correlationId, String queueNameToReceive) throws IOException {
        CompletableFuture<String> future = new CompletableFuture<>();

        String consumerTag = channel.basicConsume(
                queueNameToReceive,
                true,
                handleDeliverCallBack(future, gson, correlationId),
                consumer -> {}
        );

        try {
            // Espera a resposta do callback
            return future.get(30, TimeUnit.SECONDS); // Timeout de 30 segundos
        } catch (TimeoutException e) {
            return "Timeout while waiting for response";
        } catch (Exception e) {
            return "Error while processing response: " + e.getMessage();
        } finally {
            try {
                channel.basicCancel(consumerTag); // Cancela o consumidor antes de liberar o canal
            } catch (IOException e) {
                System.err.println("Erro ao cancelar o consumidor: " + e.getMessage());
            }
            connectionPool.releaseChannel(channel); // Libera o canal de volta ao pool
        }
    }

    public DeliverCallback handleDeliverCallBack(CompletableFuture<String> future, Gson gson, String correlationId) {
        return (consumerTag, delivery) -> {
            try {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                BudgetResponse finalResponse = gson.fromJson(message, BudgetResponse.class);

                String receivedCorrelationId = delivery.getProperties().getCorrelationId();

                if (!correlationId.equals(receivedCorrelationId)) {
                    System.err.println("CorrelationId mismatch. Ignoring message.");
                    return;
                }

                System.out.println("Reponse Received -> { " + finalResponse.getMessage() + " }");
                future.complete(finalResponse.getMessage());
            } catch (Exception e) {
                System.err.println("Error receiving message from rabbitmq " + e);
                future.completeExceptionally(e);
            }
        };
    }
}
