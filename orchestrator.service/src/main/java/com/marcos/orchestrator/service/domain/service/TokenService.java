package com.marcos.orchestrator.service.domain.service;

import com.auth0.jwt.interfaces.Claim;
import com.marcos.orchestrator.service.domain.config.infra.secrets.SecuritySecrets;

import java.util.Map;

public interface TokenService {

    String generateToken(String login);

    String validateToken(String token);

    String generateServiceToken(String serviceName);

    String validateServiceToken(String token);

    String extractClaim(String token, String claim);

    Map<String, Claim> extractClaim(String token);

    SecuritySecrets getSecrets();
}
