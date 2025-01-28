package com.marcos.auth.service.domain.service.impl;

import com.marcos.auth.service.domain.service.RabbitMQService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQServiceImpl implements RabbitMQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }
}
