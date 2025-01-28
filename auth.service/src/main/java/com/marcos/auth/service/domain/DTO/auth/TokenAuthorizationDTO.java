package com.marcos.auth.service.domain.DTO.auth;

import java.util.Map;

public record TokenAuthorizationDTO(Boolean isValid, Map<String, Object> claims) {
}
