package com.marcos.usermanagement.service.domain.model;

public enum Perfil {
    ADMINISTRADOR("admin"),
    USER("user");

    private String role;

    Perfil(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
