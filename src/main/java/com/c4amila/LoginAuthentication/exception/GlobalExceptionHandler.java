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
    @ExceptionHandler(RuntimeException.class)//exceção capturada do Service
    public ResponseEntity<ErroResponseDTO> tratarRuntimeException(RuntimeException e){
        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro na requisição",
                e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    //exceção para campos mal preenchidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacaoDosCampos(MethodArgumentNotValidException m){
        Map<String, String> erros = new HashMap<>();

        for (FieldError fieldError : m.getBindingResult().getFieldErrors()){
            erros.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErroResponseDTO erro = new ErroResponseDTO(LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Campos inválidos ou ausentes",
                erros.toString());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }
}
