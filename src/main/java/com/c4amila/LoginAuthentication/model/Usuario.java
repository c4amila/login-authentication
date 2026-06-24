package com.c4amila.LoginAuthentication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(name = "nome_completo")
    private String nomeCompleto;

    @Past(message = "A data de nascimento deve ser uma data no passado")
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "^\\d{11}$", message = "O telefone deve seguir o padrão 11999998888")
    @Column(name = "telefone")
    private String telefone;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Insira um endereço de email válido")
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Column(name = "senha", length = 100)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&*+=])(?=\\S+$).{8,}$",
    message = "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula, um caractere especial (!@#$%&*+=) e no mínimo 8 carateres")
    private String senha;

    @Column(name = "tentativas_senha", nullable = false)
    private Integer tentativaSenha = 0;

    @Column(name = "horario_bloqueio")
    private LocalDateTime horarioBloqueio;

    @Column(name = "esta_bloqueado", nullable = false)
    private Boolean estaBloqueado = false;
}
