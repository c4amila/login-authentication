package com.c4amila.LoginAuthentication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCadastroResponseDTO {
    private Long id;
    private String nomeCompleto;
    private String dataNascimento;
    private String email;
    private String telefone;
}
