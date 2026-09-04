package com.c4amila.LoginAuthentication.exception;


import com.c4amila.LoginAuthentication.dto.ErroResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacaoDosCampos(MethodArgumentNotValidException m){
        Map<String, String> erros = new HashMap<>();
        for (FieldError fieldError : m.getBindingResult().getFieldErrors()){
            erros.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Campos inválidos ou ausentes",
                erros.toString());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarExcecaoInesperada(Exception e){
        logger.error("Erro inesperado não tratado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Ocorreu um erro, tente novamente mais tarde");
    }

    private ResponseEntity<ErroResponseDTO> resposta(HttpStatus status, String titulo, String mensagem){
        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(),
                status.value(),
                titulo,
                mensagem);
        return ResponseEntity.status(status).body(erro);
    }
}
