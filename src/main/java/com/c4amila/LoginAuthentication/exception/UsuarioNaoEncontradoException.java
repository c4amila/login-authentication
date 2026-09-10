package com.c4amila.LoginAuthentication.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(String mensagem){
        super(mensagem);
    }
}
