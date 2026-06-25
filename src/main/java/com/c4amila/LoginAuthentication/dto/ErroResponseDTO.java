package com.c4amila.LoginAuthentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ErroResponseDTO {
    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
}
