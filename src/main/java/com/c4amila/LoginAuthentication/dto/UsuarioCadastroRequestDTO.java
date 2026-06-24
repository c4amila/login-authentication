package com.c4amila.LoginAuthentication.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCadastroRequestDTO {
    @NotBlank(message = "Nome é obrigatório")
    private String nomeCompleto;

    @Past(message = "A data de nascimento deve ser uma data no passado")
    private LocalDate dataNascimento;

    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "^\\d{11}$", message = "O telefone deve seguir o padrão 11999998888")
    private String telefone;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Insira um endereço de email válido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*+=])(?=\\S+$).{8,}$",
            message = "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um caractere especial (!@#$%&*+=) e no mínimo 8 carateres")
    private String senha;
}
