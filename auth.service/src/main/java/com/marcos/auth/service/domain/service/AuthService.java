package com.marcos.auth.service.domain.service;


import com.marcos.auth.service.domain.DTO.auth.AuthResponseToClientDTO;
import com.marcos.auth.service.domain.DTO.auth.AuthorizationDTO;
import com.marcos.auth.service.domain.DTO.auth.TokenAuthorizationDTO;
import com.marcos.auth.service.domain.DTO.user.UserDTO;
import com.marcos.auth.service.domain.exception.InvalidUserOrPassword;

public interface AuthService {


    AuthResponseToClientDTO auth(AuthorizationDTO authorization) throws InvalidUserOrPassword;

    Object createUser(UserDTO userDTO);

    TokenAuthorizationDTO validateToken(String tokenRequestDTO);
}
