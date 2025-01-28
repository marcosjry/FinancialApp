package com.marcos.orchestrator.service.domain.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.marcos.orchestrator.service.domain.config.infra.secrets.SecuritySecrets;
import com.marcos.orchestrator.service.domain.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    public SecuritySecrets getSecrets() {
        return secrets;
    }

    @Autowired
    private SecuritySecrets secrets;

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    @Override
    public String generateToken(String userID) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secrets.getSecret());
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(userID)
                    .withClaim("role", "ROLE_USER")
                    .withExpiresAt(Date.from(genExpirationDate()))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }

    @Override
    public String validateServiceToken(String token) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secrets.getServicesecret());
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException exception) {
            System.err.println("Token verification failed: " + exception.getMessage());
            return "";
        }
    }

    @Override
    public String generateServiceToken(String serviceName) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(secrets.getServicesecret());
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(serviceName)
                    .withClaim("role", "ROLE_SERVICE")
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while generating token", exception);
        }
    }


    @Override
    public String validateToken(String token) {
        try{
            Algorithm algorithm = Algorithm.HMAC256(secrets.getSecret());
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException exception) {
            System.err.println("Token verification failed: " + exception.getMessage());
            return "";
        }
    }

    @Override
    public String extractClaim(String token, String claim) {
        String secret = secrets.getSecret();
        if(validateServiceToken(token).equals("Authentication-Service") || validateServiceToken(token).equals("User-Budget-Service")) {
            secret = secrets.getServicesecret();
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var decodedJWT = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
            return decodedJWT.getClaim(claim).asString(); // Retorna "ROLE_SERVICE" ou outro papel
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Error while extracting role from token", exception);
        }
    }

    @Override
    public Map<String, Claim> extractClaim(String token) {
        String secret = secrets.getSecret();
        if(validateServiceToken(token).equals("Authentication-Service") || validateServiceToken(token).equals("User-Budget-Service")) {
            secret = secrets.getServicesecret();
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var decodedJWT = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
            return decodedJWT.getClaims(); // Retorna as Claims do token
        } catch (JWTVerificationException exception) {
            return new HashMap<>();
        }
    }


}
