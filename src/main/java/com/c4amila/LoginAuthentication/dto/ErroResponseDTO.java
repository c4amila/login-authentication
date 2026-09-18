package com.c4amila.LoginAuthentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErroResponseDTO {
    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private Object mensagem;
}
