package com.marcos.auth.service.domain.DTO.user;

import java.util.List;

public record LoginResponse(String userId, Boolean validUser, String email, List<String> authorities) {
}
