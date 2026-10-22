package com.c4amila.LoginAuthentication.repository;

import com.c4amila.LoginAuthentication.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, UUID> {
}
