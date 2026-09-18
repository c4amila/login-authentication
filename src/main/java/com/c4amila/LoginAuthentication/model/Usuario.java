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

    @NotNull(message = "A data de nascimento é obrigatória")
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

    @Column(name = "codigo_recuperacao")
    private String codigoRecuperacao;

    @Column(name = "cod_recuperacao_expira_em")
    private LocalDateTime codRecuperacaoExpiraEm;

    @Column(name = "tentativas_login", nullable = false)
    private Integer tentativaLogin = 0;

    @Column(name = "login_bloqueado_ate")
    private LocalDateTime loginBloqueadoAte;

    @Column(name = "horario_expiracao_codigo")
    private LocalDateTime horarioExpiracaoCodigo;

    @Column(name = "tentativa_recuperacao", nullable = false)
    private Integer tentativasRecuperacao = 0;

    @Column(name = "cod_recuperacao_bloqueado_ate")
    private LocalDateTime CodRecuperacaoBloqueadoAte;
}
