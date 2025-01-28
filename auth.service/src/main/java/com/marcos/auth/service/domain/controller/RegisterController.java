package com.marcos.auth.service.domain.controller;

import com.marcos.auth.service.domain.DTO.user.UserDTO;
import com.marcos.auth.service.domain.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping
public class RegisterController {

    @Autowired
    private AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<Object> newUser(@RequestBody UserDTO user) {
        var createdUser = this.authService.createUser(user);
        if(createdUser.toString().contains("Erro inesperado")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createdUser);
        }
        if(createdUser.toString().equals("Usuário criado com sucesso.")) {
            return ResponseEntity.accepted().body(createdUser);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(createdUser);
    }
}
