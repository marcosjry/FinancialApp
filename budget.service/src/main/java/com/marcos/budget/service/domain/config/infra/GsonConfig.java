package com.marcos.budget.service.domain.config.infra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.marcos.budget.service.domain.DTO.request.RequestBudget;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(RequestBudget.class, new CustomRequestBudgetDeserializer())
                .create();
    }
}
