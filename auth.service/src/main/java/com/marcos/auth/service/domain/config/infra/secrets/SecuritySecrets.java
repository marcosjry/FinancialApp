package com.marcos.auth.service.domain.config.infra.secrets;

public class SecuritySecrets {
    private String secret;
    private String servicesecret;

    // Getters e setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServicesecret() {
        return servicesecret;
    }

    public void setServicesecret(String servicesecret) {
        this.servicesecret = servicesecret;
    }
}
