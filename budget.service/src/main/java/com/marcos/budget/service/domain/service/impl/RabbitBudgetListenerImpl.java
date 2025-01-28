package com.marcos.budget.service.domain.service.impl;

import com.google.gson.Gson;
import com.marcos.budget.service.domain.config.infra.ConnectionPool;
import com.marcos.budget.service.domain.model.Budget;
import com.marcos.budget.service.domain.service.BudgetService;
import com.marcos.budget.service.domain.service.RabbitBudgetListener;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class RabbitBudgetListenerImpl implements RabbitBudgetListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private ConnectionPool connectionPool;

    @Autowired
    private Gson gson;

    private Channel channel;

    @PostConstruct
    @Override
    public void rabbitUserAuthenticaded() {

        try {

            channel = connectionPool.acquireChannel();

            channel.basicConsume("orchestrator_to_budget", true, (consumerTag, delivery) -> {

                // Obtendo o correlationId
                String correlationId = delivery.getProperties().getCorrelationId();

                String userId = new String(delivery.getBody(), StandardCharsets.UTF_8);

                Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
                Page<Budget> pageBudget = this.budgetService.findBudgets(userId, pageable);

                List<Budget> budgetList = pageBudget.getContent();

                String messageToOrchestrator = gson.toJson(budgetList);

                // Preparando a mensagem de resposta
                AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                        .correlationId(correlationId) // Incluindo o correlationId na resposta
                        .build();

                // Publicando na fila de resposta
                channel.basicPublish("", "budget_to_orchestrator", props, messageToOrchestrator.getBytes(StandardCharsets.UTF_8));

                System.out.println("Message sent -> { " + messageToOrchestrator + " }");

            }, consumerTag -> {
                // Callback para cancelamento
                System.out.println("Consumer canceled: " + consumerTag);
            });

            System.out.println("Listening for messages on 'orchestrator_to_budget'...");

        } catch (Exception e) {
            System.err.println("Error while consuming messages: " + e.getMessage());
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
}
