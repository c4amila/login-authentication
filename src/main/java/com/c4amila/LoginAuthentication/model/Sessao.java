package com.c4amila.LoginAuthentication.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "revogada_em", nullable = false)
    private LocalDateTime revogadaEm;

    public Boolean estaAtiva(){
        return revogadaEm == null && expiraEm.isAfter(LocalDateTime.now());
    }
}
