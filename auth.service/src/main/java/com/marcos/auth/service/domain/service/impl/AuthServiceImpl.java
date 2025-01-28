package com.marcos.auth.service.domain.service.impl;

import com.google.gson.Gson;
import com.marcos.auth.service.domain.DTO.auth.AuthResponseDTO;
import com.marcos.auth.service.domain.DTO.auth.AuthResponseToClientDTO;
import com.marcos.auth.service.domain.DTO.auth.AuthorizationDTO;
import com.marcos.auth.service.domain.DTO.auth.TokenAuthorizationDTO;
import com.marcos.auth.service.domain.DTO.rabbitmq.AuthUser;
import com.marcos.auth.service.domain.DTO.user.UserDTO;
import com.marcos.auth.service.domain.exception.InvalidUserOrPassword;
import com.marcos.auth.service.domain.service.AuthService;
import com.marcos.auth.service.domain.service.RabbitMQService;
import com.marcos.auth.service.domain.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private RabbitMQService rabbitMQService;

    @Autowired
    private TokenService tokenService;

    @Value("${user.management.service.url}")
    private String userManagementServiceUrl;

    @Override
    public AuthResponseToClientDTO auth(AuthorizationDTO authorization) throws InvalidUserOrPassword {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(authorization);

            System.out.println(json);

            //Envia o objeto convertido com o tipo que o RabbitMQ consiga entender
            HttpClient httpClient = HttpClient.newHttpClient();
            String serviceToken = this.tokenService.generateServiceToken("Authentication-Service");

            String urlParameter = userManagementServiceUrl + "/authorization/login";

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(urlParameter))
                    .header("Authorization", "Bearer " + serviceToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            AuthResponseDTO loginResponse = gson.fromJson(jsonResponse, AuthResponseDTO.class);

            if(!loginResponse.response().validUser()) {
                return new AuthResponseToClientDTO("", loginResponse.response());
            }

            String correlationId = UUID.randomUUID().toString();
            AuthUser authUser = new AuthUser(correlationId, loginResponse.response().userId());

            String message = gson.toJson(authUser);

            this.rabbitMQService.getRabbitTemplate().convertAndSend("authenticated_user", message.getBytes(StandardCharsets.UTF_8));

            var token = this.tokenService.generateToken(loginResponse.response().userId());
            return new AuthResponseToClientDTO(token, loginResponse.response());

        }   catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("Falha na Autenticação: " + e.getMessage());
        }
    }



    @Override
    public Object createUser(UserDTO userDTO) {
        boolean isValid = this.verifyUserDTOcredential(userDTO);
        if(isValid) {
            try {
                HttpClient httpClient = HttpClient.newHttpClient();
                String serviceToken = this.tokenService.generateServiceToken("Authentication-Service");

                Gson gson = new Gson();
                String jsonRequest = gson.toJson(userDTO);

                String urlParameter = userManagementServiceUrl + "/authorization/sign-up";

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(urlParameter))
                        .header("Authorization", "Bearer " + serviceToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                        .build();
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                return response.body();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return "Credenciais inválidas.";
    }

    private boolean verifyUserDTOcredential(UserDTO userDTO) {
        // Verifica se todos os campos do DTO estão vazios ou nulos
        return (
                isNullOrEmpty(userDTO.celular()) &&
                        isNullOrEmpty(userDTO.cep()) &&
                        isNullOrEmpty(userDTO.complemento()) &&
                        isNullOrEmpty(userDTO.email()) &&
                        isNullOrEmpty(userDTO.numeroResidencia()) &&
                        isNullOrEmpty(userDTO.password()) &&
                        isNullOrEmpty(userDTO.primeiroNome()) &&
                        isNullOrEmpty(userDTO.segundoNome())
        );
    }

    private boolean isNullOrEmpty(String value) {
        return value != null && !value.isEmpty();
    }

    @Override
    public TokenAuthorizationDTO validateToken(String tokenRequestDTO) {
        String subject = this.tokenService.validateToken(tokenRequestDTO);
        if(subject.isEmpty()) {
            return new TokenAuthorizationDTO(false, Collections.emptyMap());
        }
        var claims = this.tokenService.extractClaim(tokenRequestDTO);
        Map<String, Object> claimMap = new HashMap<>();
        claims.forEach((key, value) -> {
            if ("exp".equals(key)) {
                // Converte o valor numérico para uma string
                claimMap.put(key, String.valueOf(value.asLong()));
            } else {
                // Trata os demais valores como strings
                claimMap.put(key, value.asString());
            };
        });
        return new TokenAuthorizationDTO(true , claimMap);
    }

}
