package com.c4amila.LoginAuthentication.exception;

public class CredenciaisInvalidasException extends RuntimeException{
    public CredenciaisInvalidasException(String mensagem){
        super(mensagem);
    }
}
