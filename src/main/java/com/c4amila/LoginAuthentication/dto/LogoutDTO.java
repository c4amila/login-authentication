package com.c4amila.LoginAuthentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutDTO {
    @NotBlank(message = "O e-mail é obrigatório")
    private String email;
}
