package com.marcos.auth.service.domain.exception;

public class InvalidUserOrPassword extends Exception{
    public InvalidUserOrPassword(String message){
        super(message);
    }
}
