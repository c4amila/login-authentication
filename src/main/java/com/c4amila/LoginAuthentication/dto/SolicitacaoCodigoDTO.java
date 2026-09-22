package com.c4amila.LoginAuthentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecuperacaoSolicitacaoDTO {

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Insira um e-mail vállido")
    private String email;
}
