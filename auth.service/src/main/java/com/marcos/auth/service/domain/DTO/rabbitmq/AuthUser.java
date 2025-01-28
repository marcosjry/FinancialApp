package com.marcos.auth.service.domain.DTO.rabbitmq;

public record AuthUser(String correlationId, String userId) {
}
