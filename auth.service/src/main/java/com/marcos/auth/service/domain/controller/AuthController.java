package com.marcos.auth.service.domain.controller;


import com.marcos.auth.service.domain.DTO.auth.AuthorizationDTO;
import com.marcos.auth.service.domain.DTO.auth.TokenRequestDTO;
import com.marcos.auth.service.domain.DTO.user.UserExceptionDTO;
import com.marcos.auth.service.domain.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthorizationDTO authorization) throws Exception {
        var auth = authService.auth(authorization);
        if(auth.user().validUser()) {
            return ResponseEntity.status(200).body("token: "+auth.token());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UserExceptionDTO("Usuário ou Senha Inválido."));
    }

    @PostMapping("/token")
    public ResponseEntity<Object> authToken(@RequestBody TokenRequestDTO tokenRequestDTO) {
        System.out.println(tokenRequestDTO);
        var auth = authService.validateToken(tokenRequestDTO.token());
        return ResponseEntity.accepted().body(auth);
    }



}


