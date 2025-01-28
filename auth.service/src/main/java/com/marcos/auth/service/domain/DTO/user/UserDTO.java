package com.marcos.auth.service.domain.DTO.user;

public record UserDTO (
        String primeiroNome,
        String segundoNome,
        String password,
        String email,
        String celular,
        String cep,
        String numeroResidencia,
        String complemento
) {}
