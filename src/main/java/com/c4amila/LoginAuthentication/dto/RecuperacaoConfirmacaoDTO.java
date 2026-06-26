package com.c4amila.LoginAuthentication.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecuperacaoConfirmacaoDTO {
    @NotBlank(message = "O e-mail é obrigatório")
    private String email;

    @NotBlank(message = "O código de verificação é obrigatório")
    private String codigo;

    @NotBlank(message = "A nova senha é obrigatória")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*+=])(?=\\S+$).{8,}$",
            message = "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um caractere especial (!@#$%&*+=) e no mínimo 8 carateres")
    private String novaSenha;

    @NotBlank(message = "A confirmação da senha é obrigatória")
    private String confirmarNovaSenha;
}
