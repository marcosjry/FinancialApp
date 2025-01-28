package com.marcos.orchestrator.service.domain.config.infra;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue receive_from_budget() {
        return new Queue("budget_to_orchestrator", false); // Fila persistente
    }
    @Bean
    public Queue send_budget_user_to_orchestrator() {
        return new Queue("orchestrator_to_budget", false);
    }

    @Bean
    public Queue received_login_authenticated() {
        return new Queue("authenticated_user", false); // Fila persistente
    }

    @Bean
    public Queue request_from_user() {
        return new Queue("orchestrator_from_user_request", true);
    }

    @Bean
    public Queue response_to_request_orchestrator() {
        return new Queue("budget_response_to_user_request", true);
    }


}
