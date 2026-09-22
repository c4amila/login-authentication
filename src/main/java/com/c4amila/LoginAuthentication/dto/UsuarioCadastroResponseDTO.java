package com.c4amila.LoginAuthentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioCadastroResponseDTO {
    private String mensagem;
    private UsuarioResponseDTO usuario;
}
