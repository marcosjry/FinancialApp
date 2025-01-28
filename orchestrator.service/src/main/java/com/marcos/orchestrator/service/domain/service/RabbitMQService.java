package com.marcos.orchestrator.service.domain.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public interface RabbitMQService {

    RabbitTemplate getRabbitTemplate();
}
