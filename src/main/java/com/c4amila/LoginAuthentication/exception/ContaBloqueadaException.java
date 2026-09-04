package com.c4amila.LoginAuthentication.exception;

public class ContaBloqueadaException extends RuntimeException{
    public ContaBloqueadaException(String mensagem){
        super(mensagem);
    }
}
