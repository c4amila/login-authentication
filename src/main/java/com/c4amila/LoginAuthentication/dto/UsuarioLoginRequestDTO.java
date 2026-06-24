package com.c4amila.LoginAuthentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioLoginRequestDTO {
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Insira um endereço de email válido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}
