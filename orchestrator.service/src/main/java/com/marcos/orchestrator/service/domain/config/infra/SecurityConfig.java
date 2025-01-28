package com.marcos.orchestrator.service.domain.config.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcos.orchestrator.service.domain.config.infra.secrets.SecuritySecrets;
import com.marcos.orchestrator.service.domain.service.TokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilter securityFilter(TokenService tokenService) {
        return new SecurityFilter(tokenService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, SecurityFilter securityFilter) throws Exception {
        return httpSecurity
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/acesso-negado");
                        }))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/").permitAll()// Permite o acesso ao login
                        .requestMatchers(HttpMethod.POST, "/api/create/budget").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/create/transaction").hasRole("USER")
                        .anyRequest().authenticated() // Exige autenticação para outras rotas
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecuritySecrets securitySecrets() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Carrega o arquivo do classpath (src/main/resources)
            Resource resource = new ClassPathResource("secrets.json");
            // Lê o conteúdo do arquivo como InputStream
            return mapper.readValue(resource.getInputStream(), SecuritySecrets.class);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível carregar o arquivo de secrets", e);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Adicione todas as origens necessárias
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:8083"
        ));

        // Permite mais headers comumente necessários
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Permite credenciais (importante para autenticação)
        corsConfig.setAllowCredentials(true);

        // Expõe o header Authorization
        corsConfig.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

}