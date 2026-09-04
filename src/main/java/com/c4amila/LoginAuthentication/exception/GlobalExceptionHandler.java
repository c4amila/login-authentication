package com.c4amila.LoginAuthentication.exception;


import com.c4amila.LoginAuthentication.dto.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailCadastradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarEmailJaCadastrado(EmailCadastradoException e){
        return resposta(HttpStatus.CONFLICT, "E-mail já cadastrado", e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponseDTO> tratarCredenciaisInvalidas(CredenciaisInvalidasException e){
        return resposta(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", e.getMessage());
    }

    @ExceptionHandler(ContaBloqueadaException.class)
    public ResponseEntity<ErroResponseDTO> tratarContaBloqueada(ContaBloqueadaException e){
        return resposta(HttpStatus.LOCKED, "Conta bloqueada", e.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponseDTO> tratarRequisicaoInvalida(RequisicaoInvalidaException e){
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage());
    }


    private ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String titulo, String mensagem){
        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(),
                status.value(),
                titulo,
                mensagem);
        return ResponseEntity.status(status).body(erro);
    }
}
