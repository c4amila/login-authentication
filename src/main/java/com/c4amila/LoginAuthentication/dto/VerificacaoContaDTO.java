package com.c4amila.LoginAuthentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class VerificacaoContaDTO {
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Insira um e-mail válido")
    private String email;

    @NotBlank(message = "O código de verificação é obrigatório")
    @Pattern(regexp = "^\\d{6}$", message = "O código deve ter 6 dígitos")
    private String codigo;
}
