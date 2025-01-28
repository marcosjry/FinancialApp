package com.marcos.auth.service.domain.DTO.auth;


import com.marcos.auth.service.domain.DTO.user.LoginResponse;

public record AuthResponseDTO(String message, LoginResponse response) {

}
